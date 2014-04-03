/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.packager;

import it.infodreams.syncpath.report.Report;
import it.infodreams.syncpath.report.ReportItem;
import it.infodreams.syncpath.report.ReportItem.ItemSyncStatus;
import it.infodreams.syncpath.report.ReportItem.ItemType;
import it.infodreams.syncpath.services.ErrorManager;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *
 * @author mauropesci
 */
public class Packager {
    
    static final long DEFAULT_PACKAGE_SPLIT_SIZE = 1024 * 1024;
    static final String DESCRIPTOR_FILENAME = "Package.xml";
    
    private long packageSplitSize = DEFAULT_PACKAGE_SPLIT_SIZE;
    
    public void setPackageSplitSize(long size) { packageSplitSize = size; }
    public long getPackageSplitSize() { return packageSplitSize; }
    
    public void packFiles(String sourcePath, String destPath) throws FileNotFoundException {
        if (sourcePath == null) throw new IllegalArgumentException();
        
        Report report = Report.createReport(sourcePath);
        
        File sourcePathFile = new File(sourcePath);
        
        if (!sourcePathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + sourcePath + "' is not a valid source directory.", ErrorManager.ErrorLevel.SEVERE);   
        }
        
        if (destPath == null) throw new IllegalArgumentException();
        
        File destPathFile = new File(destPath);
        
        if (!destPathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + destPath + "' is not a valid destination directory for packing files.", ErrorManager.ErrorLevel.SEVERE);   
        }
        
        // Calculates the overall size of data to be packed
        long packageSize = 0;
        
        for (ReportItem item : report.items) {
            if ((item.getSyncStatus() == ItemSyncStatus.Added ||
                item.getSyncStatus() == ItemSyncStatus.Modified) &&
                item.getType() != ItemType.Directory) packageSize += item.getSize();
        }
        
        long splitsCount = (packageSplitSize > 0 ? (long) Math.ceil((float)packageSize / (float)packageSplitSize) : 1);
        
        // Creates the package descriptor
        PackageDescriptor descriptor = new PackageDescriptor();
        descriptor.setSplitsCount(splitsCount);
        
        // Write de descriptor into the destination directory
        try (XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(destPath + File.separator + DESCRIPTOR_FILENAME) ) )) {
            encoder.writeObject(descriptor);
        }        
        
        long packagedSize = 0;
        long splitIndex = 0;
        long lastSplitIndex = 0;
        String splitDirName = "";    
        String actualDestinationDirName = "";
        
        Report splitReport = new Report();
        
        for (ReportItem item : report.items) {     
            splitDirName = descriptor.getSplitDirPrefix() + splitIndex;
               
            if  (item.getType() == ItemType.Directory) {
                actualDestinationDirName = splitDirName + File.separator + item.getPath(false);                                
                continue;
            }                           
               
            // Calculates the value to assign to the split name
            splitIndex = (packageSplitSize <= 0 ? 1 : (long) Math.ceil(packagedSize / packageSplitSize));                        
             
            if (item.getSyncStatus() != ItemSyncStatus.Deleted &&
                item.getType() != ItemType.Directory) {
                packagedSize += item.getSize();

                // And make a copy of the files
                String filename = item.getPath(false) + item.getName();
                String destFilename = destPath + File.separator + splitDirName + File.separator;

                Path sourceFile = new File(sourcePath + File.separator + filename).toPath();
                Path destFile = new File(destFilename + filename).toPath();

                try 
                {
                    Files.createDirectories(destFile);                        
                    Files.copy(sourceFile, destFile, COPY_ATTRIBUTES, REPLACE_EXISTING);
                } catch (IOException ex) {
                    ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);
                }
            }
            
            // New split was been encountered...
            if (splitIndex != lastSplitIndex) {
                // ... then saves the report file for the split
                String destReportPath = destPath + File.separator + splitDirName;
                
                try {                
                    Files.createDirectories(new File(destReportPath).toPath());
                } catch (IOException ex) {
                    ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);
                }
                splitReport.saveToFile(destReportPath + File.separator + "____Report.xml");                                
                splitReport.items.clear();                
            }                     
            
            lastSplitIndex = splitIndex;

            splitReport.items.add(item);            
        }
        
                String destReportPath = destPath + File.separator + splitDirName;
                
                try {                
                    Files.createDirectories(new File(destReportPath).toPath());
                } catch (IOException ex) {
                    ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);
                }
                splitReport.saveToFile(destReportPath + File.separator + "____Report.xml");        
    }
    
    public void unpackFiles(String sourcePath, String destPath) {
         if (sourcePath == null) throw new IllegalArgumentException();
        
        File sourcePathFile = new File(sourcePath);
        
        if (!sourcePathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + sourcePath + "' is not a valid package source directory.", ErrorManager.ErrorLevel.SEVERE);   
        }
        
        if (destPath == null) throw new IllegalArgumentException();
        
        File destPathFile = new File(destPath);
        
        if (!destPathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + destPath + "' is not a valid destination directory for unpacking files.", ErrorManager.ErrorLevel.SEVERE);   
        }       
    }
 
}
