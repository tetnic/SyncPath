/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.report;

import it.infodreams.syncpath.services.ErrorManager;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mauropesci
 */
public class Report {
     
    public static Report loadFromFile(String filename) throws FileNotFoundException {
        if (filename == null) throw new IllegalArgumentException();
        
        File file = new File(filename);
        if (!file.isFile()) ErrorManager.getInstance().error("File '" + filename + "' is not a valid report file.", ErrorManager.ErrorLevel.SEVERE);
        
        Report report;
        try (XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( new FileInputStream(filename) ) )) {
            report = new Report();
            report.items.addAll((List<ReportItem>) decoder.readObject());        
        } 
        
        return report;
    }
    
    public static Report createDiffReport(Report source, Report destination) {
        /* 
         * The following could appear a unoptimal solution to check for
         * differences between two lists. For the moment, considering 
         * the fact that directories have little differences, the resulting
         * throughput is quite acceptable.
         */
        List<ReportItem> sourceItems = new ArrayList<>(source.items);
        List<ReportItem> destItems = new ArrayList<>(destination.items);
        
        List<ReportItem> addedItems = new ArrayList<>();
        List<ReportItem> modifiedItems = new ArrayList<>();
        List<ReportItem> removedItems = new ArrayList<>();
        
        for (ReportItem srcItem : sourceItems) {
            for (ReportItem dstItem : destItems) {
                if (srcItem.isSame(dstItem)) {
                    // Check if it's modified
                    if (srcItem.getLastModified() != dstItem.getLastModified() ||
                        srcItem.getSize() != dstItem.getSize()) {
                        modifiedItems.add(srcItem);
                        srcItem.setItemStatus(ReportItem.ItemSyncStatus.Modified);
                    }
                    
                    // Remove items from source lists so at the end of
                    // the process will be simple to identify added and 
                    // removed items
                    sourceItems.remove(srcItem);
                    destItems.remove(dstItem);
                    break;
                }
            }
        }
        
        // Copy every remaining source items into the 'added items' array
        // and set items status as Added
        for (ReportItem item : sourceItems) {
            addedItems.add(item); 
            item.setItemStatus(ReportItem.ItemSyncStatus.Added);
        }
        
        // Copy every remaining dest items into the 'deleted items' array
        // and set items status as Deleted
        for (ReportItem item : sourceItems) {
            removedItems.add(item);
            item.setItemStatus(ReportItem.ItemSyncStatus.Deleted);
        }
        
        /*
         * Now we have every info to generate the final report
         */
        Report finalReport = new Report();
        finalReport.items.addAll(addedItems);
        finalReport.items.addAll(modifiedItems);        
        finalReport.items.addAll(removedItems);
        
        return finalReport;
    }
    
    public static Report createReport(String path) {
        Report report = new Report();
        report.scanDirectory(path, null);
        
        return report;
    }    
    
    public final List<ReportItem> items = new ArrayList<>();
 
    private Report() {
        
    }
   
    public void saveToFile(String filename) throws FileNotFoundException {
        if (filename == null) throw new IllegalArgumentException();
        
        try (XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(filename) ) )) {
            encoder.writeObject(items);
        }
    }
    
    public void scanDirectory(String path, ReportItem parent) {
        File directory = new File(path);         
        
        if (!directory.isDirectory()) ErrorManager.getInstance().error("Directory '" + path + "' is not a valid directory to scan.", ErrorManager.ErrorLevel.SEVERE);        
        
        ReportItem newparent = new ReportItem((parent != null ? parent : null), directory.getName(), ReportItem.ItemType.Directory);        
        
        for (File file : directory.listFiles()) {
            ReportItem item = new ReportItem(newparent, file.getName(), file.isDirectory() ? ReportItem.ItemType.Directory : ReportItem.ItemType.File);
            items.add(item);
            
            if (file.isDirectory()) scanDirectory(file.getAbsolutePath(), newparent);            
        }
    }   
    
}
