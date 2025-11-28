package com.tiqs.controller;

import com.tiqs.auth.AuthContextHolder;
import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.entity.CloudFile;
import com.tiqs.service.CloudFileService;
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
public class CloudFileController {

    private final CloudFileService cloudFileService;

    public CloudFileController(CloudFileService cloudFileService) {
        this.cloudFileService = cloudFileService;
    }

    @RequireRole(UserRole.TEACHER)
    @PostMapping("/upload")
    public ApiResponse<CloudFile> uploadFile(
            @RequestParam("classId") Long classId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
            @RequestParam(value = "folderId", required = false) Long folderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("请求上传云盘文件 classId={} fileName={} userId={} folderId={}", classId, file.getOriginalFilename(), userId, folderId);

        CloudFile cloudFile = cloudFileService.uploadFile(classId, userId, file, description, isPublic, folderId);
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
            files = cloudFileService.listByFolder(folderId);
        } else if (uploaderId != null) {
            files = cloudFileService.listByClassAndUploader(classId, uploaderId);
        } else if (fileType != null) {
            files = cloudFileService.listByClassAndFileType(classId, fileType);
        } else {
            files = cloudFileService.listByClass(classId);
        }

        return ApiResponse.ok(files);
    }

    @GetMapping("/files/{id}")
    public ApiResponse<CloudFile> getFile(@PathVariable Long id) {
        log.debug("获取云盘文件详情 id={}", id);
        CloudFile cloudFile = cloudFileService.get(id);
        return ApiResponse.ok(cloudFile);
    }

    @PutMapping("/files/{id}")
    public ApiResponse<CloudFile> updateFile(
            @PathVariable Long id,
            @RequestBody CloudFile cloudFile) {

        Long userId = AuthContextHolder.get().userId();
        cloudFile.setId(id);
        log.info("更新云盘文件信息 id={} userId={}", id, userId);

        CloudFile updatedFile = cloudFileService.update(cloudFile, userId);
        return ApiResponse.ok(updatedFile);
    }

    @DeleteMapping("/files/{id}")
    public ApiResponse<Void> deleteFile(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.warn("删除云盘文件 id={} userId={}", id, userId);

        cloudFileService.delete(id, userId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.info("下载云盘文件 id={} userId={}", id, userId);

        CloudFile cloudFile = cloudFileService.get(id);
        byte[] fileContent = cloudFileService.downloadFile(id, userId);

        ByteArrayResource resource = new ByteArrayResource(fileContent);

        String encodedFileName = URLEncoder.encode(cloudFile.getOriginalFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @GetMapping("/statistics")
    public ApiResponse<CloudFileService.CloudFileStatistics> getStatistics(
            @RequestParam("classId") Long classId) {

        log.debug("获取云盘统计信息 classId={}", classId);
        CloudFileService.CloudFileStatistics statistics = cloudFileService.getStatistics(classId);
        return ApiResponse.ok(statistics);
    }

    @PutMapping("/files/{id}/move")
    public ApiResponse<CloudFile> moveFile(
            @PathVariable Long id,
            @RequestParam(value = "folderId", required = false) Long folderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("移动文件 id={} folderId={} userId={}", id, folderId, userId);

        CloudFile cloudFile = cloudFileService.moveFile(id, folderId, userId);
        return ApiResponse.ok(cloudFile);
    }
}