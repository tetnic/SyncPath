/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath;

import it.infodreams.syncpath.commands.Commander;

/**
 *
 * @author mauropesci
 */
public class SyncPath {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {  
/*        Packager packager = new Packager();
        packager.scanPath("./mageia4", "Mageia4Report.xml");*/
        Commander commander = new Commander();
        commander.parseArgs(args);
    }
    
}
