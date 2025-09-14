package com.server.client.model;

import java.io.Serializable;
import java.text.DecimalFormat;

public class FileItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String type;
    private long size;
    private String category;
    private String uploadDate;
    private String path;
    
    public FileItem() {
        this.uploadDate = new java.util.Date().toString();
    }
    
    public FileItem(String name, String type, long size, String category, String path) {
        this();
        this.name = name;
        this.type = type;
        this.size = size;
        this.category = category;
        this.path = path;
    }
    
    // Format file size in human readable format
    public String getFormattedSize() {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        return decimalFormat.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    // Get file extension
    public String getExtension() {
        if (name != null && name.contains(".")) {
            return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        }
        return "";
    }
    
    // Check if file is image
    public boolean isImage() {
        String ext = getExtension();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
               ext.equals("gif") || ext.equals("bmp") || ext.equals("svg");
    }
    
    // Check if file is document
    public boolean isDocument() {
        String ext = getExtension();
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
               ext.equals("txt") || ext.equals("rtf");
    }
    
    // Get CSS class for file type icon (if you want to add icons)
    public String getFileTypeClass() {
        String ext = getExtension();
        switch (ext) {
            case "pdf": return "file-pdf";
            case "doc":
            case "docx": return "file-word";
            case "xls":
            case "xlsx": return "file-excel";
            case "ppt":
            case "pptx": return "file-powerpoint";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif": return "file-image";
            case "mp3":
            case "wav": return "file-audio";
            case "mp4":
            case "avi": return "file-video";
            case "zip":
            case "rar": return "file-archive";
            default: return "file-generic";
        }
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString() {
        return "FileItem{" +
               "name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", size=" + size +
               ", category='" + category + '\'' +
               ", uploadDate='" + uploadDate + '\'' +
               ", path='" + path + '\'' +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        FileItem fileItem = (FileItem) o;
        
        if (size != fileItem.size) return false;
        if (name != null ? !name.equals(fileItem.name) : fileItem.name != null) return false;
        if (type != null ? !type.equals(fileItem.type) : fileItem.type != null) return false;
        return category != null ? category.equals(fileItem.category) : fileItem.category == null;
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }
}