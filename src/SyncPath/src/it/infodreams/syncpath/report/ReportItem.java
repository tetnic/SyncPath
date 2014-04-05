/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.infodreams.syncpath.report;

import it.infodreams.syncpath.services.ErrorManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private String md5;
    
    public ReportItem() {        
    }
    
    public ReportItem(ReportItem parent, String name, ItemType type, String path) {                           
        setParent(parent);
        setName(name);
        setType(type);
        setSyncStatus(ItemSyncStatus.Added);
        
        if (type != ItemType.Directory) {
            String filename = path + File.separator + (type == ItemType.File ? name : "");       
            Path file = new File(filename).toPath();
            
            BasicFileAttributes attr = null;
            try {
                attr = Files.readAttributes(file, BasicFileAttributes.class);
            } catch (IOException ex) {
                ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);
                return;
            }
        
            lastModifiedTime = attr.lastModifiedTime().toMillis();
            size = attr.size();
            
            try {
                md5 = getMD5Checksum(filename);
            } catch (Exception ex) {
                ErrorManager.getInstance().error(ex.getMessage(), ErrorManager.ErrorLevel.NOT_SEVERE);               
                md5 = null;
            } 
        }      
    }
    
    private byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        
        return complete.digest();
    }

    private String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
       
        return result;
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
    
    public void setMD5(String md5) { this.md5 = md5; }
    
    public ReportItem getParent() { return parent; }
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public ItemSyncStatus getSyncStatus() { return status; }
    public long getLastModified() { return lastModifiedTime; }
    public long getSize() { return size; }
    public String getMD5() { return md5; }
    
    public ItemSyncStatus getItemStatus() { return status; }
    protected void setItemStatus(ItemSyncStatus status) { this.status = status; }
    
    /*
     * This operation is useful to avoid the insertion of duplicated
     * files or directories into a report.
     */
    public boolean isSame(ReportItem item) {
        if (item == null) throw new IllegalArgumentException();
        return item.getType() == type &&
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
