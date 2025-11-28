package com.tiqs.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudFolder {
    private Long id;
    private Long classId;
    private String name;
    private Long parentFolderId;
    private String path;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联属性
    private CloudFolder parentFolder;
    private List<CloudFolder> subFolders;
    private List<CloudFile> files;
    private String creatorName;
    private Integer fileCount;
    private Integer folderCount;

    public CloudFolder(Long classId, String name, Long parentFolderId, String path, Long creatorId) {
        this.classId = classId;
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.path = path;
        this.creatorId = creatorId;
    }

    public boolean isRoot() {
        return parentFolderId == null;
    }

    public String getFullPath() {
        if (isRoot()) {
            return "/" + name;
        }
        return path + "/" + name;
    }
}