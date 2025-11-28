/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CloudFileService
 */
package com.tiqs.service;

import com.tiqs.entity.CloudFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudFileService {
    CloudFile uploadFile(Long classId, Long uploaderId, MultipartFile file, String description, Boolean isPublic, Long folderId);

    List<CloudFile> listByClass(Long classId);

    List<CloudFile> listByClassAndFileType(Long classId, String fileType);

    List<CloudFile> listByClassAndUploader(Long classId, Long uploaderId);

    List<CloudFile> listByFolder(Long folderId);

    CloudFile moveFile(Long fileId, Long folderId, Long userId);

    CloudFile get(Long id);

    CloudFile update(CloudFile cloudFile, Long userId);

    void delete(Long id, Long userId);

    void incrementDownloadCount(Long id);

    byte[] downloadFile(Long id, Long userId);

    CloudFileStatistics getStatistics(Long classId);

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
}