/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.services;

/**
 *
 * @author mauropesci
 */
public class ErrorManager {
    public enum ErrorLevel {
        SEVERE,
        NOT_SEVERE
    }
    
    private static final ErrorManager instance = new ErrorManager();
    
    public static final ErrorManager getInstance() { return instance; }
    
    public void error(String message, ErrorLevel level) {
        System.out.println((level == ErrorLevel.SEVERE ? "Severe " : "") + "Error : " + message);
        if (level == ErrorLevel.SEVERE) System.exit(-1);
    }
}
