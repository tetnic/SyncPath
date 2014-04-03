/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.application.report;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author mauropesci
 */
public class Report {
     
    public static Report loadFromFile(String filename) throws FileNotFoundException {
        if (filename == null) throw new IllegalArgumentException();
        
        XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( new FileInputStream(filename) ) );
        Report report = new Report();
        report.items.addAll((List<ReportItem>) decoder.readObject());
        decoder.close();        
        
        return report;
    }
    
    public static Report createDiffReport(Report source, Report destination) {
        return null;
    }
    
    public static Report createReport(String path) {
        Report report = new Report();
        report.scanDirectory(path, null);
        
        return report;
    }    
    
    private final List<ReportItem> items = new ArrayList<>();
 
    private Report() {
        
    }
   
    public void saveToFile(String filename) throws FileNotFoundException {
        if (filename == null) throw new IllegalArgumentException();
        
        XMLEncoder encoder = new XMLEncoder( new BufferedOutputStream( new FileOutputStream(filename) ) );
        encoder.writeObject(items);
        encoder.close();
    }
    
    public void scanDirectory(String path, ReportItem parent) {
        File directory = new File(path);              
        if (!directory.isDirectory()) throw new IllegalArgumentException();
        
        ReportItem newparent = new ReportItem((parent != null ? parent : null), directory.getName(), ReportItem.ItemType.Directory);        
        
        for (File file : directory.listFiles()) {
            ReportItem item = new ReportItem(newparent, file.getName(), file.isDirectory() ? ReportItem.ItemType.Directory : ReportItem.ItemType.File);
            items.add(item);
            
            if (file.isDirectory()) scanDirectory(file.getAbsolutePath(), newparent);            
            else {
               
            }
        }
    }   
    
}
