/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.commands;

import it.infodreams.syncpath.packager.Packager;
import it.infodreams.syncpath.report.Report;
import it.infodreams.syncpath.services.ErrorManager;
import it.infodreams.syncpath.services.ErrorManager.ErrorLevel;
import it.infodreams.syncpath.services.LogManager;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author mauropesci
 */
public class Commander {
    
    private final Options options = new Options();
    
    public Commander() {
        Option help = new Option("help", "Print this help message");
        Option verbose = new Option("verbose", "Show more output information during process");
        Option version = new Option("version", "Show the version information");
        
        Option scan = OptionBuilder.withLongOpt("scan")                                
                                  .withDescription("Attempts full scan of a specified path and creates a report")
                                  .create("c");
       
        Option pack = OptionBuilder.withLongOpt("pack")
                                  .withDescription("Perform a packing process")
                                  .create("p");

        Option unpack = OptionBuilder.withLongOpt("unpack")
                                  .withDescription("Perform a packing process")
                                  .create("u");

        Option sourcePath = OptionBuilder.withArgName("path")
                                        .hasArg()
                                        .withDescription("Path to use as source for operations.")
                                        .withLongOpt("source-path")
                                        .create("s");
        
        Option destPath = OptionBuilder.withArgName("path")
                                        .hasArg()
                                        .withDescription("Path to use as destionation for operations.")
                                        .withLongOpt("dest-path")
                                        .create("d");        
        
        Option reportFile = OptionBuilder.withArgName("filename")
                                        .hasArg()
                                        .withDescription("Report to use as source for packing operations.")
                                        .withLongOpt("report")
                                        .create("r");         
       
        Option splitSize = OptionBuilder.withArgName("size-in-bytes")
                                        .hasArg()
                                        .withDescription("Size of each splitted package (0 for unlimited size).")
                                        .withLongOpt("split-size")
                                        .create("t");
        
        options.addOption(help);
        options.addOption(verbose);
        options.addOption(version);
        options.addOption(scan);   
        options.addOption(sourcePath);
        options.addOption(destPath);  
        options.addOption(reportFile);          
        options.addOption(pack);
        options.addOption(splitSize);        
        options.addOption(unpack);       
    }
    
    public void parseArgs( String[] args ) {
        CommandLineParser parser = new BasicParser();
        Packager packager = new Packager();
        
        try {
            CommandLine line = parser.parse(options, args);
            
            if (line.hasOption("split-size")) {
                long size = 0;
                
                if (line.hasOption("split-size")) {
                    size = Long.parseLong(line.getOptionValue("split-size"));
                } else {
                    ErrorManager.getInstance().error("Value expected for parameter 'split-size'", ErrorLevel.SEVERE);
                }
                
                packager.setPackageSplitSize(size);            
            }
            
            if (line.hasOption("verbose")) {
                LogManager.getInstance().setVerbosityLevel(LogManager.LOG_LEVEL_2);
            }
            
            if (line.hasOption("help") || args.length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("SyncPath", options);   
            } else if (line.hasOption("scan")) {
                
                String path = null;
                String reportFileName = null;
                
                if ( line.hasOption("source-path")) {
                    path = line.getOptionValue("source-path");
                } else {
                    ErrorManager.getInstance().error("Parameter '--scan' needs a source path to perform its operation", ErrorLevel.SEVERE);
                }
                
                if ( line.hasOption("report")) {
                    reportFileName = line.getOptionValue("report");
                } else {
                    ErrorManager.getInstance().error("Parameter '--scan' needs a report filename to perform its operation", ErrorLevel.SEVERE);
                }
                                
                packager.scanPath(path, reportFileName);
                
            } else if (line.hasOption("pack")) {
                
                String sourcePath = null;
                String destPath = null;
                String reportFileName = null;
                
                if ( line.hasOption("source-path")) {
                    sourcePath = line.getOptionValue("source-path");
                } else {
                    ErrorManager.getInstance().error("Parameter '--pack' needs a source path to perform its operation", ErrorLevel.SEVERE);
                }
                
                if ( line.hasOption("dest-path")) {
                    destPath = line.getOptionValue("dest-path");
                } else {
                    ErrorManager.getInstance().error("Parameter '--pack' needs a source path to perform its operation", ErrorLevel.SEVERE);
                }
                                
                if ( line.hasOption("report")) {
                    reportFileName = line.getOptionValue("report");
                } else {
                    ErrorManager.getInstance().error("Parameter '--pack' needs a report filename to perform its operation", ErrorLevel.SEVERE);
                }
                               
                Report report;
                
                try {
                    report = Report.loadFromFile(reportFileName);
                    packager.packFiles(report, sourcePath, destPath);
                } catch (FileNotFoundException ex) {
                    ErrorManager.getInstance().error(ex.getMessage(), ErrorLevel.SEVERE);
                }
                
            } else if (line.hasOption("unpack")) {
                
            }
        } catch (ParseException ex) {
            ErrorManager.getInstance().error(ex.getMessage(), ErrorLevel.SEVERE);
        }
    }
    
}
