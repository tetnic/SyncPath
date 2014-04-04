/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.report;

import it.infodreams.syncpath.services.ErrorManager;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mauropesci
 */
public final class ReportItem implements Serializable {
    
    /*
     * The status indicates what kind of synchronization must be
     * operated on the item. Added files will be simply copied into
     * the destination folder, Modified files will be replaced where
     * Deleted files will be removed on the destination path.
     */
    public enum ItemSyncStatus {
        Added,
        Modified,
        Deleted
    }
    
    public enum ItemType {
        File,
        Directory
    }
    
    private ReportItem parent;
    private String name;
    private ItemType type;
    private ItemSyncStatus status;
    private long lastModifiedTime;
    private long size;
    
    public ReportItem() {        
    }
    
    public ReportItem(ReportItem parent, String name, ItemType type) {        
        setParent(parent);
        setName(name);
        setType(type);
        setSyncStatus(ItemSyncStatus.Added);
        
        if (type != ItemType.Directory) {
            String filename = getPath(true) + (type == ItemType.File ? name : "");       
            File file = new File(filename);
        
            if (!file.exists()) ErrorManager.getInstance().error("File \'" +  filename + "\' doesn't exist.", ErrorManager.ErrorLevel.SEVERE);
        
            lastModifiedTime = file.lastModified();
            size = file.length();                
        }
        
        System.out.println((type == ItemType.File ? "File : " : "Directory : ") + name + " => " + getPath(true));                 
    }
    
    public void setParent(ReportItem parent) {
        if (parent != null && parent.getType() != ItemType.Directory) throw new IllegalArgumentException();  
        if (this.parent != null) return;
        this.parent = parent;
    }
    
    public void setName(String name) {
        if (name == null) throw new IllegalArgumentException();
        if (this.name != null) return;
        this.name = name;
    }
    
    public void setType(ItemType type) {
        if (type == null) throw new IllegalArgumentException();
        if (this.type != null) return;
        this.type = type;
    }
    
    public void setSyncStatus(ItemSyncStatus status) {
        this.status = status;
    }
    
    public void setLastModified(long time) { 
        lastModifiedTime = time;
    }
    
    public void setSize(long size) { 
        this.size = size; 
    }    
    
    public ReportItem getParent() { return parent; }
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public ItemSyncStatus getSyncStatus() { return status; }
    public long getLastModified() { return lastModifiedTime; }
    public long getSize() { return size; }
    
    public ItemSyncStatus getItemStatus() { return status; }
    protected void setItemStatus(ItemSyncStatus status) { this.status = status; }
    
    /*
     * This operation is useful to avoid the insertion of duplicated
     * files or directories into a report.
     */
    public boolean isSame(ReportItem item) {
        if (item == null) throw new IllegalArgumentException();
        return item.getType() != type &&
               item.getName().equals(name);        
    }
    
    public String getPath(boolean reportRoot) {
        List<String> pathItems = new ArrayList<>();
        ReportItem item = this;
        
        do {
            if (item.getType() != ItemType.File) pathItems.add(0, item.getName());
            item = item.getParent();
            if (item == null) break;
        } while (true);
        
        if (!reportRoot) pathItems.remove(0);
        
        StringBuilder path = new StringBuilder();        
        for (String element : pathItems) {
            path.append(element);
            path.append(File.separator);
        }
            
        return path.toString();
    }
    
}
