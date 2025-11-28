/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CloudFolderService
 */
package com.tiqs.service;

import com.tiqs.entity.CloudFolder;

import java.util.List;

public interface CloudFolderService {

    CloudFolder createFolder(Long classId, String name, Long parentFolderId, Long creatorId);

    CloudFolder getFolder(Long id);

    List<CloudFolder> getRootFolders(Long classId);

    List<CloudFolder> getSubFolders(Long classId, Long parentFolderId);

    List<CloudFolder> getFoldersWithStats(Long classId, Long parentFolderId);

    CloudFolder renameFolder(Long id, String newName, Long userId);

    CloudFolder moveFolder(Long id, Long newParentId, Long userId);

    void deleteFolder(Long id, Long userId);

    List<CloudFolder> searchFolders(Long classId, String keyword);

    String buildFolderPath(Long parentFolderId);

    boolean isFolderNameExists(Long classId, String name, Long parentFolderId);

    List<CloudFolder> getFolderTree(Long classId, Long rootFolderId);

    CloudFolderStatistics getStatistics(Long classId);

    record CloudFolderStatistics(Long totalSize, Integer fileCount, Integer folderCount) {

        public String getFormattedTotalSize() {
                if (totalSize == null) return "0 B";
                long size = totalSize;
                if (size < 1024) return size + " B";
                if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
                if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
                return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
            }
        }
}