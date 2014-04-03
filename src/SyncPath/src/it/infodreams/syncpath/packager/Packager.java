/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.packager;

import it.infodreams.syncpath.report.Report;
import it.infodreams.syncpath.report.ReportItem;
import it.infodreams.syncpath.services.ErrorManager;
import java.io.File;

/**
 *
 * @author mauropesci
 */
public class Packager {
    
    static final long DEFAULE_PACKAGE_SPLIT_SIZE = 0;
    
    private long packageSplitSize = DEFAULE_PACKAGE_SPLIT_SIZE;
    
    public void packFiles(String sourcePath, String destPath) {
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
