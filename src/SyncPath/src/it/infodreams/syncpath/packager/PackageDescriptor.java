/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.packager;

/**
 *
 * @author mauropesci
 */
public class PackageDescriptor {
    
    private static final String DEFAULT_SPLIT_PREFIX = "Split";
 
    private long splitsCount;
    private String splitDirPrefix = DEFAULT_SPLIT_PREFIX;
    
    public void setSplitsCount(long count) { 
        if (count <= 0) throw new IllegalArgumentException();
        splitsCount = count; 
    }
    
    public long getSplitsCount() { return splitsCount; }
    
    public void setSplitDirPrefix(String prefix) {
        if (prefix == null) throw new IllegalArgumentException();
        splitDirPrefix = prefix;
    }
    
    public String getSplitDirPrefix() { return splitDirPrefix; }
    
}
