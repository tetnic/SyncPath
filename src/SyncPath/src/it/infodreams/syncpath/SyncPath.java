/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath;

import it.infodreams.syncpath.application.report.Report;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mauropesci
 */
public class SyncPath {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Report report = Report.createReport("build");
        
        try {
            report.saveToFile("Report.xml");
            Report readReport = Report.loadFromFile("Report.xml");
            int i = 0;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SyncPath.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
