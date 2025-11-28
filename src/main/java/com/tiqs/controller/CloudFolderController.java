/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CloudFolderController
 */
package com.tiqs.controller;

import com.tiqs.auth.AuthContextHolder;
import com.tiqs.auth.RequireRole;
import com.tiqs.auth.UserRole;
import com.tiqs.common.ApiResponse;
import com.tiqs.entity.CloudFolder;
import com.tiqs.service.CloudFolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cloud/folders")
public class CloudFolderController {

    private final CloudFolderService cloudFolderService;

    public CloudFolderController(CloudFolderService cloudFolderService) {
        this.cloudFolderService = cloudFolderService;
    }

    @RequireRole(UserRole.TEACHER)
    @PostMapping
    public ApiResponse<CloudFolder> createFolder(
            @RequestParam("classId") Long classId,
            @RequestParam("name") String name,
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("创建文件夹 classId={} name={} parentFolderId={} userId={}", classId, name, parentFolderId, userId);

        CloudFolder folder = cloudFolderService.createFolder(classId, name, parentFolderId, userId);
        return ApiResponse.ok(folder);
    }

    @GetMapping("/root")
    public ApiResponse<List<CloudFolder>> getRootFolders(@RequestParam("classId") Long classId) {
        log.debug("获取根文件夹列表 classId={}", classId);
        List<CloudFolder> folders = cloudFolderService.getRootFolders(classId);
        return ApiResponse.ok(folders);
    }

    @GetMapping("/{id}")
    public ApiResponse<CloudFolder> getFolder(@PathVariable Long id) {
        log.debug("获取文件夹详情 id={}", id);
        CloudFolder folder = cloudFolderService.getFolder(id);
        return ApiResponse.ok(folder);
    }

    @GetMapping("/{id}/subfolders")
    public ApiResponse<List<CloudFolder>> getSubFolders(@PathVariable Long id) {
        log.debug("获取子文件夹列表 parentId={}", id);
        CloudFolder parent = cloudFolderService.getFolder(id);
        List<CloudFolder> folders = cloudFolderService.getFoldersWithStats(parent.getClassId(), id);
        return ApiResponse.ok(folders);
    }

    @GetMapping("/{id}/tree")
    public ApiResponse<List<CloudFolder>> getFolderTree(@PathVariable Long id) {
        log.debug("获取文件夹树 id={}", id);
        CloudFolder folder = cloudFolderService.getFolder(id);
        List<CloudFolder> tree = cloudFolderService.getFolderTree(folder.getClassId(), id);
        return ApiResponse.ok(tree);
    }

    @PutMapping("/{id}/rename")
    public ApiResponse<CloudFolder> renameFolder(
            @PathVariable Long id,
            @RequestParam("name") String name) {

        Long userId = AuthContextHolder.get().userId();
        log.info("重命名文件夹 id={} name={} userId={}", id, name, userId);

        CloudFolder folder = cloudFolderService.renameFolder(id, name, userId);
        return ApiResponse.ok(folder);
    }

    @PutMapping("/{id}/move")
    public ApiResponse<CloudFolder> moveFolder(
            @PathVariable Long id,
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {

        Long userId = AuthContextHolder.get().userId();
        log.info("移动文件夹 id={} parentFolderId={} userId={}", id, parentFolderId, userId);

        CloudFolder folder = cloudFolderService.moveFolder(id, parentFolderId, userId);
        return ApiResponse.ok(folder);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFolder(@PathVariable Long id) {
        Long userId = AuthContextHolder.get().userId();
        log.warn("删除文件夹 id={} userId={}", id, userId);

        cloudFolderService.deleteFolder(id, userId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/search")
    public ApiResponse<List<CloudFolder>> searchFolders(
            @RequestParam("classId") Long classId,
            @RequestParam("keyword") String keyword) {

        log.debug("搜索文件夹 classId={} keyword={}", classId, keyword);
        List<CloudFolder> folders = cloudFolderService.searchFolders(classId, keyword);
        return ApiResponse.ok(folders);
    }

    @GetMapping("/statistics")
    public ApiResponse<CloudFolderService.CloudFolderStatistics> getStatistics(
            @RequestParam("classId") Long classId) {

        log.debug("获取文件夹统计信息 classId={}", classId);
        CloudFolderService.CloudFolderStatistics statistics = cloudFolderService.getStatistics(classId);
        return ApiResponse.ok(statistics);
    }
}