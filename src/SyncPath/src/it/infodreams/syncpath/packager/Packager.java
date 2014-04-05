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
import it.infodreams.syncpath.services.LogManager;
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
    
    public void scanPath(String path, String reportFileName) {
        if (path == null) throw new IllegalArgumentException();
        if (reportFileName == null) throw new IllegalArgumentException();
                      
        Report report = Report.createReport(path);               
        
        try {
            report.saveToFile(reportFileName);                            
        } catch (FileNotFoundException ex) {
            ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.SEVERE);
        }
    }
    
    public void packFiles(Report report, String sourcePath, String destPath) throws FileNotFoundException {
        if (report == null) throw new IllegalArgumentException();
        if (sourcePath == null) throw new IllegalArgumentException();
        
        LogManager.getInstance().log("Starting packing process from source path '" + sourcePath + "' to destination path '" + destPath + "'", LogManager.LOG_LEVEL_1);        
        
        File sourcePathFile = new File(sourcePath);
        
        if (!sourcePathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + sourcePath + "' is not a valid source directory.", ErrorManager.ErrorLevel.SEVERE);   
        }
        
        Report sourceReport = Report.createReport(sourcePath);
        report = Report.createDiffReport(report, sourceReport);
        
        if (destPath == null) throw new IllegalArgumentException();
        
        File destPathFile = new File(destPath);
        
        if (!destPathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + destPath + "' is not a valid destination directory for packing files.", ErrorManager.ErrorLevel.SEVERE);   
        }       
        
        // Calculates the overall size of data to be packed
        long packageSize = 0;
        long packageFiles = 0;
        
        for (ReportItem item : report.items) {
            if ((item.getSyncStatus() == ItemSyncStatus.Added ||
                item.getSyncStatus() == ItemSyncStatus.Modified) &&
                item.getType() != ItemType.Directory)  {
                packageSize += item.getSize();
                packageFiles++;
            }
        }
        
        long splitsCount = (packageSplitSize > 0 ? (long) Math.ceil((float)packageSize / (float)packageSplitSize) : 1);
        
        if (splitsCount == 0) {
            // Operation completed
            LogManager.getInstance().log("No files found to be aligned, nothing to do.", LogManager.LOG_LEVEL_1);                     
            return;
        }
        
        LogManager.getInstance().log("Found " + report.items.size() + " for an amount of " + packageSize + " bytes.", LogManager.LOG_LEVEL_1);   
        LogManager.getInstance().log("Split size is set to " + packageSplitSize + " for a total of " + splitsCount + " splits.", LogManager.LOG_LEVEL_1);   
                    
        // Creates the package descriptor
        PackageDescriptor descriptor = new PackageDescriptor();
        descriptor.setSplitsCount(splitsCount);
        
        LogManager.getInstance().log("Writing package descriptor.", LogManager.LOG_LEVEL_1);
        
        // Write de descriptor into the destination directory
        try (XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(destPath + File.separator + DESCRIPTOR_FILENAME) ) )) {
            encoder.writeObject(descriptor);
        }        
        
        long fileCounter = 0;
        long packagedSize = 0;
        long splitIndex = 0;
        long lastSplitIndex = 0;
        String splitDirName = "";    
        String actualDestinationDirName = "";
        
        LogManager.getInstance().log("Copying files...", LogManager.LOG_LEVEL_1); 
        
        Report splitReport = new Report();
        
        for (ReportItem item : report.items) {     
            splitDirName = descriptor.getSplitDirPrefix() + splitIndex;
               
            if  (item.getType() == ItemType.Directory) {
                actualDestinationDirName = splitDirName + File.separator + item.getPath(false);                                
                continue;
            }                                                                          
            
            fileCounter++;            
            LogManager.getInstance().log("(" + fileCounter + "-" + packageFiles + ") Copying file : " + item.getPath(true) + item.getName(), LogManager.LOG_LEVEL_2);            
            
            // Calculates the value to assign to the split name
            splitIndex = (packageSplitSize <= 0 ? 1 : (long) Math.ceil(packagedSize / packageSplitSize));                        
            if (splitIndex != lastSplitIndex) splitIndex = lastSplitIndex + 1; 
            
            if (item.getSyncStatus() != ItemSyncStatus.Deleted &&
                item.getType() != ItemType.Directory) {
                packagedSize += item.getSize();

                // And make a copy of the files
                String filename = item.getPath(false) + item.getName();
                String destFilename = destPath + File.separator + splitDirName + File.separator;

                File destFile = new File(destFilename + filename);                                
                
                Path sourceFilePath = new File(sourcePath + File.separator + filename).toPath();
                Path destFilePath = destFile.toPath();                                                

                try 
                {
                    Files.deleteIfExists(destFilePath);                    
                    Files.createDirectories(destFilePath);                        
                    Files.copy(sourceFilePath, destFilePath, COPY_ATTRIBUTES, REPLACE_EXISTING);
                } catch (IOException ex) {
                    ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);
                }
            }
            
            // New split was been encountered...
            if (splitIndex != lastSplitIndex) {
                // ... then saves the report file for the split
                saveToFile(splitReport, destPath, splitDirName);                             
                splitReport.items.clear();                
            }                     
            
            lastSplitIndex = splitIndex;

            splitReport.items.add(item);            
        }              
        
        saveToFile(splitReport, destPath, splitDirName);  
        
        LogManager.getInstance().log("Packing process complete.", LogManager.LOG_LEVEL_1);                
    }
     
    private void saveToFile(Report splitReport, String destPath, String splitDirName) throws FileNotFoundException {        
        String destReportPath = destPath + File.separator + splitDirName;
        
        try {                
            Files.createDirectories(new File(destReportPath).toPath());
        } catch (IOException ex) {
            ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);
        }
                
        splitReport.saveToFile(destReportPath + File.separator + "____Report_" + splitDirName + ".xml");                
    }
    
    public void packFiles(String sourcePath, String destPath) throws FileNotFoundException {
        if (sourcePath == null) throw new IllegalArgumentException();
        
        File sourcePathFile = new File(sourcePath);
        
        if (!sourcePathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + sourcePath + "' is not a valid source directory.", ErrorManager.ErrorLevel.SEVERE);   
        }
        
        if (destPath == null) throw new IllegalArgumentException();
        
        File destPathFile = new File(destPath);
        
        if (!destPathFile.isDirectory()) {
            ErrorManager.getInstance().error("Filename '" + destPath + "' is not a valid destination directory for packing files.", ErrorManager.ErrorLevel.SEVERE);   
        }
        
        Report report = Report.createReport(sourcePath);        
        packFiles(report, sourcePath, destPath);
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
        
        // Reads the package descriptor
        
        // For each split identified 
        
        // Loads the report and copy files to the dest path
    }

}
