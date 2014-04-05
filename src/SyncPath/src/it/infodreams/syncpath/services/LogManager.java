/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.services;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author mauropesci
 */
public class LogManager {
    public static final int NO_LOG = 0;
    public static final int LOG_LEVEL_1 = 1;
    public static final int LOG_LEVEL_2 = 2;
    public static final int LOG_LEVEL_3 = 3;

    private static final LogManager instance = new LogManager();
    
    public static final LogManager getInstance() { return instance; }
    
    private int verbosityLevel = NO_LOG;
    
    public void setVerbosityLevel(int level) { verbosityLevel = level; }
    public int getVerbosityLevel() { return verbosityLevel; }
    
    public void log(String message, int level) {
        if (level < 0 || level > verbosityLevel) return;
        
        Date date = new Date();
        
        System.out.println("[" + new Timestamp(date.getTime()) + "] : " + message);
    }    
}
