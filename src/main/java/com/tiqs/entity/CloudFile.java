package com.tiqs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudFile {
    private Long id;
    private Long classId;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String description;
    private Long uploaderId;
    private Integer downloadCount;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String uploaderName;
    private String uploaderRole;
    private Long folderId;
    private String folderPath;
    private String folderName;
    
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
    
    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    public boolean isImage() {
        String ext = getFileExtension();
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
               ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }
    
    public boolean isDocument() {
        String ext = getFileExtension();
        return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx") || 
               ext.equals("txt") || ext.equals("rtf") || ext.equals("odt");
    }
    
    public boolean isSpreadsheet() {
        String ext = getFileExtension();
        return ext.equals("xls") || ext.equals("xlsx") || ext.equals("csv") || 
               ext.equals("ods");
    }
    
    public boolean isPresentation() {
        String ext = getFileExtension();
        return ext.equals("ppt") || ext.equals("pptx") || ext.equals("odp");
    }
    
    public boolean isArchive() {
        String ext = getFileExtension();
        return ext.equals("zip") || ext.equals("rar") || ext.equals("7z") || 
               ext.equals("tar") || ext.equals("gz");
    }
    
    public String getFileCategory() {
        if (isImage()) return "图片";
        if (isDocument()) return "文档";
        if (isSpreadsheet()) return "表格";
        if (isPresentation()) return "演示文稿";
        if (isArchive()) return "压缩包";
        return "其他";
    }
}