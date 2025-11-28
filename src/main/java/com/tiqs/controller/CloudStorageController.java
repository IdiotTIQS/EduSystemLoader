/**
 * @author TIQS
 * @date Created in 2025-11-28 14:25:49
 * @description CloudStorageController - Unified controller for cloud files and folders
 */
package com.tiqs.controller;

import com.tiqs.auth.AuthContextHolder;
import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.entity.CloudFile;
import com.tiqs.entity.CloudFolder;
import com.tiqs.service.CloudStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cloud")
public class CloudStorageController {

    private final CloudStorageService cloudStorageService;

    public CloudStorageController(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    // --- File Endpoints ---

    @RequireRole(UserRole.TEACHER)
    @PostMapping("/files/upload")
    public ApiResponse<CloudFile> uploadFile(
            @RequestParam("classId") Long classId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
            @RequestParam(value = "folderId", required = false) Long folderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("请求上传云盘文件 classId={} fileName={} userId={} folderId={}", classId, file.getOriginalFilename(), userId, folderId);

        CloudFile cloudFile = cloudStorageService.uploadFile(classId, userId, file, description, isPublic, folderId);
        return ApiResponse.ok(cloudFile);
    }

    @GetMapping("/files")
    public ApiResponse<List<CloudFile>> listFiles(
            @RequestParam("classId") Long classId,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam(value = "folderId", required = false) Long folderId) {

        log.debug("查询云盘文件列表 classId={} fileType={} uploaderId={} folderId={}", classId, fileType, uploaderId, folderId);

        List<CloudFile> files;
        if (folderId != null) {
            files = cloudStorageService.listFilesByFolder(folderId);
        } else if (uploaderId != null) {
            files = cloudStorageService.listFilesByClassAndUploader(classId, uploaderId);
        } else if (fileType != null) {
            files = cloudStorageService.listFilesByClassAndType(classId, fileType);
        } else {
            files = cloudStorageService.listFilesByClass(classId);
        }

        return ApiResponse.ok(files);
    }

    @GetMapping("/files/{id}")
    public ApiResponse<CloudFile> getFile(@PathVariable Long id) {
        log.debug("获取云盘文件详情 id={}", id);
        CloudFile cloudFile = cloudStorageService.getFile(id);
        return ApiResponse.ok(cloudFile);
    }

    @PutMapping("/files/{id}")
    public ApiResponse<CloudFile> updateFile(
            @PathVariable Long id,
            @RequestBody CloudFile cloudFile) {

        Long userId = AuthContextHolder.get().userId();
        cloudFile.setId(id);
        log.info("更新云盘文件信息 id={} userId={}", id, userId);

        CloudFile updatedFile = cloudStorageService.updateFile(cloudFile, userId);
        return ApiResponse.ok(updatedFile);
    }

    @DeleteMapping("/files/{id}")
    public ApiResponse<Void> deleteFile(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.warn("删除云盘文件 id={} userId={}", id, userId);

        cloudStorageService.deleteFile(id, userId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.info("下载云盘文件 id={} userId={}", id, userId);

        CloudFile cloudFile = cloudStorageService.getFile(id);
        byte[] fileContent = cloudStorageService.downloadFile(id, userId);

        ByteArrayResource resource = new ByteArrayResource(fileContent);

        String encodedFileName = URLEncoder.encode(cloudFile.getOriginalFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @GetMapping("/files/statistics")
    public ApiResponse<CloudStorageService.CloudFileStatistics> getFileStatistics(
            @RequestParam("classId") Long classId) {

        log.debug("获取云盘文件统计信息 classId={}", classId);
        CloudStorageService.CloudFileStatistics statistics = cloudStorageService.getFileStatistics(classId);
        return ApiResponse.ok(statistics);
    }

    @PutMapping("/files/{id}/move")
    public ApiResponse<CloudFile> moveFile(
            @PathVariable Long id,
            @RequestParam(value = "folderId", required = false) Long folderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("移动文件 id={} folderId={} userId={}", id, folderId, userId);

        CloudFile cloudFile = cloudStorageService.moveFile(id, folderId, userId);
        return ApiResponse.ok(cloudFile);
    }

    // --- Folder Endpoints ---

    @RequireRole(UserRole.TEACHER)
    @PostMapping("/folders")
    public ApiResponse<CloudFolder> createFolder(
            @RequestParam("classId") Long classId,
            @RequestParam("name") String name,
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("创建文件夹 classId={} name={} parentFolderId={} userId={}", classId, name, parentFolderId, userId);

        CloudFolder folder = cloudStorageService.createFolder(classId, name, parentFolderId, userId);
        return ApiResponse.ok(folder);
    }

    @GetMapping("/folders/root")
    public ApiResponse<List<CloudFolder>> getRootFolders(@RequestParam("classId") Long classId) {
        log.debug("获取根文件夹列表 classId={}", classId);
        List<CloudFolder> folders = cloudStorageService.getRootFolders(classId);
        return ApiResponse.ok(folders);
    }

    @GetMapping("/folders/{id}")
    public ApiResponse<CloudFolder> getFolder(@PathVariable Long id) {
        log.debug("获取文件夹详情 id={}", id);
        CloudFolder folder = cloudStorageService.getFolder(id);
        return ApiResponse.ok(folder);
    }

    @GetMapping("/folders/{id}/subfolders")
    public ApiResponse<List<CloudFolder>> getSubFolders(@PathVariable Long id) {
        log.debug("获取子文件夹列表 parentId={}", id);
        CloudFolder parent = cloudStorageService.getFolder(id);
        List<CloudFolder> folders = cloudStorageService.getFoldersWithStats(parent.getClassId(), id);
        return ApiResponse.ok(folders);
    }

    @GetMapping("/folders/{id}/tree")
    public ApiResponse<List<CloudFolder>> getFolderTree(@PathVariable Long id) {
        log.debug("获取文件夹树 id={}", id);
        CloudFolder folder = cloudStorageService.getFolder(id);
        List<CloudFolder> tree = cloudStorageService.getFolderTree(folder.getClassId(), id);
        return ApiResponse.ok(tree);
    }

    @PutMapping("/folders/{id}/rename")
    public ApiResponse<CloudFolder> renameFolder(
            @PathVariable Long id,
            @RequestParam("name") String name) {

        Long userId = AuthContextHolder.get().userId();
        log.info("重命名文件夹 id={} name={} userId={}", id, name, userId);

        CloudFolder folder = cloudStorageService.renameFolder(id, name, userId);
        return ApiResponse.ok(folder);
    }

    @PutMapping("/folders/{id}/move")
    public ApiResponse<CloudFolder> moveFolder(
            @PathVariable Long id,
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("移动文件夹 id={} parentFolderId={} userId={}", id, parentFolderId, userId);

        CloudFolder folder = cloudStorageService.moveFolder(id, parentFolderId, userId);
        return ApiResponse.ok(folder);
    }

    @DeleteMapping("/folders/{id}")
    public ApiResponse<Void> deleteFolder(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.warn("删除文件夹 id={} userId={}", id, userId);

        cloudStorageService.deleteFolder(id, userId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/folders/search")
    public ApiResponse<List<CloudFolder>> searchFolders(
            @RequestParam("classId") Long classId,
            @RequestParam("keyword") String keyword) {

        log.debug("搜索文件夹 classId={} keyword={}", classId, keyword);
        List<CloudFolder> folders = cloudStorageService.searchFolders(classId, keyword);
        return ApiResponse.ok(folders);
    }

    @GetMapping("/folders/statistics")
    public ApiResponse<CloudStorageService.CloudFolderStatistics> getFolderStatistics(
            @RequestParam("classId") Long classId) {

        log.debug("获取文件夹统计信息 classId={}", classId);
        CloudStorageService.CloudFolderStatistics statistics = cloudStorageService.getFolderStatistics(classId);
        return ApiResponse.ok(statistics);
    }
}
