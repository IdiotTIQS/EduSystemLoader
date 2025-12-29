/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CloudFile
 */
package com.tiqs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean isImage() {
        String ext = getFileExtension();
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) ||
                "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext);
    }

    public boolean isDocument() {
        String ext = getFileExtension();
        return "pdf".equals(ext) || "doc".equals(ext) || "docx".equals(ext) ||
                "txt".equals(ext) || "rtf".equals(ext) || "odt".equals(ext);
    }

    public boolean isSpreadsheet() {
        String ext = getFileExtension();
        return "xls".equals(ext) || "xlsx".equals(ext) || "csv".equals(ext) ||
                "ods".equals(ext);
    }

    public boolean isPresentation() {
        String ext = getFileExtension();
        return "ppt".equals(ext) || "pptx".equals(ext) || "odp".equals(ext);
    }

    public boolean isArchive() {
        String ext = getFileExtension();
        return "zip".equals(ext) || "rar".equals(ext) || "7z".equals(ext) ||
                "tar".equals(ext) || "gz".equals(ext);
    }

    public String getFileCategory() {
        if (isImage()) {
            return "图片";
        }
        if (isDocument()) {
            return "文档";
        }
        if (isSpreadsheet()) {
            return "表格";
        }
        if (isPresentation()) {
            return "演示文稿";
        }
        if (isArchive()) {
            return "压缩包";
        }
        return "其他";
    }
}