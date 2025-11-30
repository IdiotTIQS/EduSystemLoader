/**
 * @author TIQS
 * @date Created in 2025-11-28 14:21:52
 * @description CloudStorageService - Unified service for cloud files and folders
 */
package com.tiqs.service;

import com.tiqs.entity.CloudFile;
import com.tiqs.entity.CloudFolder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudStorageService {


    CloudFile uploadFile(Long classId, Long uploaderId, MultipartFile file, String description, Boolean isPublic, Long folderId);

    List<CloudFile> listFilesByClass(Long classId);

    List<CloudFile> listFilesByClassAndType(Long classId, String fileType);

    List<CloudFile> listFilesByClassAndUploader(Long classId, Long uploaderId);

    List<CloudFile> listFilesByFolder(Long folderId);

    CloudFile moveFile(Long fileId, Long folderId, Long userId);

    CloudFile getFile(Long id);

    CloudFile updateFile(CloudFile cloudFile, Long userId);

    void deleteFile(Long id, Long userId);

    byte[] downloadFile(Long id, Long userId);

    CloudFileStatistics getFileStatistics(Long classId);

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

    CloudFolderStatistics getFolderStatistics(Long classId);


    record CloudFileStatistics(Long totalSize, Integer fileCount) {
        public String getFormattedTotalSize() {
            if (totalSize == null) {
                return "0 B";
            }
            long size = totalSize;
            if (size < 1024) {
                return size + " B";
            }
            if (size < 1024 * 1024) {
                return String.format("%.1f KB", size / 1024.0);
            }
            if (size < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", size / (1024.0 * 1024));
            }
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    record CloudFolderStatistics(Long totalSize, Integer fileCount, Integer folderCount) {
        public String getFormattedTotalSize() {
            if (totalSize == null) {
                return "0 B";
            }
            long size = totalSize;
            if (size < 1024) {
                return size + " B";
            }
            if (size < 1024 * 1024) {
                return String.format("%.1f KB", size / 1024.0);
            }
            if (size < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", size / (1024.0 * 1024));
            }
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
