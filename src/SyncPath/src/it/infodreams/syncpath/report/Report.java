/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.report;

import it.infodreams.syncpath.report.ReportItem.ItemType;
import it.infodreams.syncpath.services.ErrorManager;
import it.infodreams.syncpath.services.LogManager;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
        
        LogManager.getInstance().log("Loading report from file '" + filename + "'...", LogManager.LOG_LEVEL_1);
        
        Report report;
        try (XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( new FileInputStream(filename) ) )) {
            report = new Report();
            report.items.addAll((List<ReportItem>) decoder.readObject());        
        } 
        
        return report;
    }
    
    public static Report createDiffReport(Report source, Report destination) {
        
        LogManager.getInstance().log("Starting diff from source and destination reports...", LogManager.LOG_LEVEL_1);        
        
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
        
        HashMap<String, ReportItem> hashedReport = new HashMap<>();        
        for (int destIndex = 0; destIndex < destItems.size(); destIndex++) {
            ReportItem dstItem = destItems.get(destIndex);            
            
            if (dstItem.getType() == ItemType.Directory) continue;
            
            String key = dstItem.getPath(true) + dstItem.getName();
            hashedReport.put(key, dstItem);
        }
        
        for (int srcIndex = 0; srcIndex < sourceItems.size(); srcIndex++) {
            ReportItem srcItem = sourceItems.get(srcIndex);   
            
            if (srcItem.getType() == ItemType.Directory) continue;
            
            String fileName = srcItem.getPath(true) +  srcItem.getName();
            ReportItem dstItem = hashedReport.get(fileName);
                        
            if (dstItem != null) {
                // Check if it's modified
                if (srcItem.getLastModified() != dstItem.getLastModified() ||
                    srcItem.getSize() != dstItem.getSize()) {
                    modifiedItems.add(srcItem);
                    srcItem.setItemStatus(ReportItem.ItemSyncStatus.Modified);

                    LogManager.getInstance().log("Modified file found : " + srcItem.getPath(true) + srcItem.getName(), LogManager.LOG_LEVEL_2);                        
                }

                // Remove items from source lists so at the end of
                // the process will be simple to identify added and 
                // removed items
                sourceItems.remove(srcIndex);
                hashedReport.remove(fileName);
                break;
            }
        }
        
        // Copy every remaining source items into the 'added items' array
        // and set items status as Added
        for (ReportItem item : sourceItems) {
            addedItems.add(item); 
            item.setItemStatus(ReportItem.ItemSyncStatus.Added);
            
            LogManager.getInstance().log("Added file found : " + item.getPath(true) + item.getName(), LogManager.LOG_LEVEL_2);                                    
        }
        
        // Copy every remaining dest items into the 'deleted items' array
        // and set items status as Deleted
        for (ReportItem item : hashedReport.values()) {
            removedItems.add(item);
            item.setItemStatus(ReportItem.ItemSyncStatus.Deleted);
            
            LogManager.getInstance().log("Removed file found : " + item.getPath(true) + item.getName(), LogManager.LOG_LEVEL_2);                                                
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
        LogManager.getInstance().log("Creating report of directory '" + path + "'...", LogManager.LOG_LEVEL_1); 
        
        Report report = new Report();                
        report.scanDirectory(path, null);      

        return report;
    }    
    
    public final List<ReportItem> items = new ArrayList<>();
 
    public Report() {
        
    }
   
    public void saveToFile(String filename) throws FileNotFoundException {
        if (filename == null) throw new IllegalArgumentException();
        
        LogManager.getInstance().log("Saving report file as '" + filename + "'.", LogManager.LOG_LEVEL_1);          
        
        try (XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(filename) ) )) {
            encoder.writeObject(items);
        }        
    }
    
    public void scanDirectory(String path, ReportItem parent) {
        File directory = new File(path);         

        LogManager.getInstance().log("Scanning directory '" + path + "'...", LogManager.LOG_LEVEL_1); 
        
        if (!directory.isDirectory()) ErrorManager.getInstance().error("Directory '" + path + "' is not a valid directory to scan.", ErrorManager.ErrorLevel.SEVERE);        

        ReportItem newparent = new ReportItem((parent != null ? parent : null), directory.getName(), ReportItem.ItemType.Directory, path);        
        
        for (File file : directory.listFiles()) {
            ReportItem item = new ReportItem(newparent, file.getName(), file.isDirectory() ? ReportItem.ItemType.Directory : ReportItem.ItemType.File, path);
            items.add(item);
            
            LogManager.getInstance().log("New item found : " + item.getPath(true) + item.getName(), LogManager.LOG_LEVEL_2);   
            
            if (file.isDirectory()) scanDirectory(file.getAbsolutePath(), newparent);            
        }      
    }   
    
}
