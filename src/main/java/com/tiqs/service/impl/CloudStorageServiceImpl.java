/**
 * @author TIQS
 * @date Created in 2025-11-28 14:21:52
 * @description CloudStorageServiceImpl
 */
package com.tiqs.service.impl;

import com.tiqs.common.BusinessException;
import com.tiqs.entity.CloudFile;
import com.tiqs.entity.CloudFolder;
import com.tiqs.mapper.CloudFileMapper;
import com.tiqs.mapper.CloudFolderMapper;
import com.tiqs.service.CloudStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CloudStorageServiceImpl implements CloudStorageService {

    private final CloudFileMapper cloudFileMapper;
    private final CloudFolderMapper cloudFolderMapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public CloudStorageServiceImpl(CloudFileMapper cloudFileMapper, CloudFolderMapper cloudFolderMapper) {
        this.cloudFileMapper = cloudFileMapper;
        this.cloudFolderMapper = cloudFolderMapper;
    }

    // --- File Operations ---

    @Transactional
    @Override
    public CloudFile uploadFile(Long classId, Long uploaderId, MultipartFile file, String description, Boolean isPublic, Long folderId) {
        if (file.isEmpty()) {
            throw BusinessException.of(400, "文件不能为空");
        }

        // 检查文件大小限制 (100MB)
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw BusinessException.of(413, "文件大小超出限制，最大支持100MB");
        }

        // 检查文件类型
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw BusinessException.of(400, "文件名不能为空");
        }

        // 检查危险文件类型
        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (isDangerousFileType(fileExtension)) {
            throw BusinessException.of(400, "不允许上传此类型的文件");
        }

        try {
            // 创建云盘目录
            String cloudDir = uploadDir + "/cloud/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path cloudPath = Paths.get(cloudDir);
            if (!Files.exists(cloudPath)) {
                Files.createDirectories(cloudPath);
            }

            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID() + "." + fileExtension;
            Path filePath = cloudPath.resolve(uniqueFileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 创建数据库记录
            CloudFile cloudFile = new CloudFile();
            cloudFile.setClassId(classId);
            cloudFile.setFileName(uniqueFileName);
            cloudFile.setOriginalFileName(originalFileName);
            cloudFile.setFilePath("/cloud/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/" + uniqueFileName);
            cloudFile.setFileSize(file.getSize());
            cloudFile.setFileType(fileExtension);
            cloudFile.setDescription(description);
            cloudFile.setUploaderId(uploaderId);
            cloudFile.setDownloadCount(0);
            cloudFile.setIsPublic(isPublic != null ? isPublic : true);
            cloudFile.setFolderId(folderId);

            cloudFileMapper.insert(cloudFile);

            log.info("文件上传成功 classId={} uploaderId={} fileName={}", classId, uploaderId, originalFileName);
            return cloudFileMapper.findById(cloudFile.getId());

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw BusinessException.of(500, "文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<CloudFile> listFilesByClass(Long classId) {
        log.debug("查询班级云盘文件 classId={}", classId);
        return cloudFileMapper.findByClassId(classId);
    }

    @Override
    public List<CloudFile> listFilesByClassAndType(Long classId, String fileType) {
        log.debug("按文件类型查询班级云盘文件 classId={} fileType={}", classId, fileType);
        return cloudFileMapper.findByClassIdAndFileType(classId, fileType);
    }

    @Override
    public List<CloudFile> listFilesByClassAndUploader(Long classId, Long uploaderId) {
        log.debug("查询用户上传的班级云盘文件 classId={} uploaderId={}", classId, uploaderId);
        return cloudFileMapper.findByClassIdAndUploader(classId, uploaderId);
    }

    @Override
    public List<CloudFile> listFilesByFolder(Long folderId) {
        return cloudFileMapper.findByFolderId(folderId);
    }

    @Override
    @Transactional
    public CloudFile moveFile(Long fileId, Long folderId, Long userId) {
        log.info("移动文件 fileId={} folderId={} userId={}", fileId, folderId, userId);

        CloudFile cloudFile = getFile(fileId);
        cloudFileMapper.moveFile(fileId, folderId);
        cloudFile.setFolderId(folderId);

        return cloudFile;
    }

    @Override
    public CloudFile getFile(Long id) {
        CloudFile cloudFile = cloudFileMapper.findById(id);
        if (cloudFile == null) {
            log.warn("云盘文件不存在 id={}", id);
            throw BusinessException.of(404, "文件不存在");
        }
        return cloudFile;
    }

    @Override
    @Transactional
    public CloudFile updateFile(CloudFile cloudFile, Long userId) {
        CloudFile existing = cloudFileMapper.findById(cloudFile.getId());
        if (existing == null) {
            log.warn("更新失败，云盘文件不存在 id={}", cloudFile.getId());
            throw BusinessException.of(404, "文件不存在");
        }

        // 只有上传者或教师可以修改
        if (!existing.getUploaderId().equals(userId)) {
            // 这里可以添加检查用户是否为该班级教师的逻辑
            throw BusinessException.of(403, "无权限修改此文件");
        }

        cloudFileMapper.update(cloudFile);
        log.info("更新云盘文件成功 id={} userId={}", cloudFile.getId(), userId);
        return cloudFileMapper.findById(cloudFile.getId());
    }

    @Override
    @Transactional
    public void deleteFile(Long id, Long userId) {
        CloudFile cloudFile = cloudFileMapper.findById(id);
        if (cloudFile == null) {
            log.warn("删除失败，云盘文件不存在 id={}", id);
            throw BusinessException.of(404, "文件不存在");
        }

        // 只有上传者或教师可以删除
        if (!cloudFile.getUploaderId().equals(userId)) {
            // 这里可以添加检查用户是否为该班级教师的逻辑
            throw BusinessException.of(403, "无权限删除此文件");
        }

        // 删除物理文件
        try {
            Path filePath = Paths.get(uploadDir + cloudFile.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            log.warn("删除物理文件失败，但数据库记录已删除 filePath={}", cloudFile.getFilePath(), e);
        }

        cloudFileMapper.delete(id);
        log.info("删除云盘文件成功 id={} userId={}", id, userId);
    }

    @Override
    public byte[] downloadFile(Long id, Long userId) {
        CloudFile cloudFile = getFile(id);

        // 检查下载权限（这里可以根据实际需求扩展）
        if (!cloudFile.getIsPublic()) {
            // 私有文件可能需要特殊权限检查
            if (!cloudFile.getUploaderId().equals(userId)) {
                // 这里可以添加检查用户是否为该班级学生的逻辑
                throw BusinessException.of(403, "无权限下载此文件");
            }
        }

        try {
            // 尝试更新下载次数，如果失败则继续下载文件
            try {
                incrementDownloadCountInNewTransaction(id);
            } catch (Exception e) {
                log.warn("更新下载次数失败，但继续下载文件: {}", e.getMessage());
            }

            Path filePath = Paths.get(uploadDir + cloudFile.getFilePath());
            if (!Files.exists(filePath)) {
                throw BusinessException.of(404, "文件不存在");
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("文件下载失败", e);
            throw BusinessException.of(500, "文件下载失败");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementDownloadCountInNewTransaction(Long id) {
        cloudFileMapper.incrementDownloadCount(id);
    }

    @Override
    public CloudFileStatistics getFileStatistics(Long classId) {
        Long totalSize = cloudFileMapper.getTotalSizeByClassId(classId);
        Integer fileCount = cloudFileMapper.getFileCountByClassId(classId);
        return new CloudFileStatistics(totalSize != null ? totalSize : 0L, fileCount != null ? fileCount : 0);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex + 1);
    }

    private boolean isDangerousFileType(String extension) {
        // 危险文件类型列表
        String[] dangerousTypes = {
                "exe", "bat", "cmd", "com", "scr", "msi", "dll", "so", "dylib",
                "sh", "bash", "zsh", "fish", "ps1", "pl", "rb", "vbs", "wsf", "jar", "app", "deb", "rpm",
                "dmg", "pkg", "iso", "img", "vmdk", "ova", "ovf"
        };

        for (String dangerousType : dangerousTypes) {
            if (extension.equals(dangerousType)) {
                return true;
            }
        }
        return false;
    }

    // --- Folder Operations ---

    @Override
    @Transactional
    public CloudFolder createFolder(Long classId, String name, Long parentFolderId, Long creatorId) {
        log.info("创建文件夹 classId={} name={} parentFolderId={} creatorId={}", classId, name, parentFolderId, creatorId);

        // 检查文件夹名称是否已存在
        if (isFolderNameExists(classId, name, parentFolderId)) {
            throw new BusinessException(400, "文件夹名称已存在");
        }

        // 如果有父文件夹，检查父文件夹是否存在
        if (parentFolderId != null) {
            CloudFolder parentFolder = cloudFolderMapper.findById(parentFolderId);
            if (parentFolder == null || !parentFolder.getClassId().equals(classId)) {
                throw new BusinessException(404, "父文件夹不存在");
            }
        }

        // 构建文件夹路径
        String parentPath = buildFolderPath(parentFolderId);
        String folderPath = parentPath + "/" + name;

        CloudFolder folder = new CloudFolder(classId, name, parentFolderId, folderPath, creatorId);
        cloudFolderMapper.insert(folder);

        return folder;
    }

    @Override
    public CloudFolder getFolder(Long id) {
        CloudFolder folder = cloudFolderMapper.findById(id);
        if (folder == null) {
            throw new BusinessException(404, "文件夹不存在");
        }
        return folder;
    }

    @Override
    public List<CloudFolder> getRootFolders(Long classId) {
        return cloudFolderMapper.findRootFolders(classId);
    }

    @Override
    public List<CloudFolder> getSubFolders(Long classId, Long parentFolderId) {
        return cloudFolderMapper.findByParentId(classId, parentFolderId);
    }

    @Override
    public List<CloudFolder> getFoldersWithStats(Long classId, Long parentFolderId) {
        return cloudFolderMapper.findWithStats(classId, parentFolderId);
    }

    @Override
    @Transactional
    public CloudFolder renameFolder(Long id, String newName, Long userId) {
        log.info("重命名文件夹 id={} newName={} userId={}", id, newName, userId);

        CloudFolder folder = getFolder(id);

        // 检查新名称是否已存在
        if (isFolderNameExists(folder.getClassId(), newName, folder.getParentFolderId())) {
            throw new BusinessException(400, "文件夹名称已存在");
        }

        cloudFolderMapper.updateName(id, newName);
        folder.setName(newName);

        return folder;
    }

    @Override
    @Transactional
    public CloudFolder moveFolder(Long id, Long newParentId, Long userId) {
        log.info("移动文件夹 id={} newParentId={} userId={}", id, newParentId, userId);

        CloudFolder folder = getFolder(id);

        // 检查是否移动到自己的子文件夹
        if (newParentId != null) {
            List<Long> subFolderIds = cloudFolderMapper.findAllSubFolderIds(id);
            if (subFolderIds.contains(newParentId)) {
                throw new BusinessException(400, "不能移动到自己的子文件夹");
            }

            CloudFolder newParent = cloudFolderMapper.findById(newParentId);
            if (newParent == null || !newParent.getClassId().equals(folder.getClassId())) {
                throw new BusinessException(404, "目标文件夹不存在");
            }

            // 检查目标文件夹中是否已存在同名文件夹
            if (isFolderNameExists(folder.getClassId(), folder.getName(), newParentId)) {
                throw new BusinessException(400, "目标文件夹中已存在同名文件夹");
            }
        }

        // 构建新路径
        String newParentPath = buildFolderPath(newParentId);
        String newPath = newParentPath + "/" + folder.getName();

        cloudFolderMapper.moveFolder(id, newParentId, newPath);
        folder.setParentFolderId(newParentId);
        folder.setPath(newPath);

        return folder;
    }

    @Override
    @Transactional
    public void deleteFolder(Long id, Long userId) {
        log.warn("删除文件夹 id={} userId={}", id, userId);

        CloudFolder folder = getFolder(id);

        // 递归删除所有子文件夹
        List<Long> subFolderIds = cloudFolderMapper.findAllSubFolderIds(id);
        subFolderIds.add(id); // 包含自己

        // 删除所有子文件夹中的文件
        for (Long folderId : subFolderIds) {
            cloudFileMapper.deleteByFolderId(folderId);
        }

        // 删除所有子文件夹
        for (Long folderId : subFolderIds) {
            cloudFolderMapper.delete(folderId);
        }
    }

    @Override
    public List<CloudFolder> searchFolders(Long classId, String keyword) {
        return cloudFolderMapper.searchByName(classId, keyword);
    }

    @Override
    public String buildFolderPath(Long parentFolderId) {
        if (parentFolderId == null) {
            return "";
        }

        CloudFolder parentFolder = cloudFolderMapper.findById(parentFolderId);
        if (parentFolder == null) {
            return "";
        }

        return parentFolder.getPath();
    }

    @Override
    public boolean isFolderNameExists(Long classId, String name, Long parentFolderId) {
        int count = cloudFolderMapper.countByName(classId, name, parentFolderId);
        return count > 0;
    }

    @Override
    public List<CloudFolder> getFolderTree(Long classId, Long rootFolderId) {
        List<CloudFolder> tree = new ArrayList<>();

        if (rootFolderId == null) {
            // 获取根文件夹
            List<CloudFolder> rootFolders = getRootFolders(classId);
            for (CloudFolder root : rootFolders) {
                buildSubTree(root);
                tree.add(root);
            }
        } else {
            CloudFolder root = getFolder(rootFolderId);
            buildSubTree(root);
            tree.add(root);
        }

        return tree;
    }

    private void buildSubTree(CloudFolder parent) {
        List<CloudFolder> children = getSubFolders(parent.getClassId(), parent.getId());
        for (CloudFolder child : children) {
            buildSubTree(child);
        }
        parent.setSubFolders(children);
    }

    @Override
    public CloudFolderStatistics getFolderStatistics(Long classId) {
        // 获取所有文件夹
        List<CloudFolder> allFolders = cloudFolderMapper.findAllByClassId(classId);
        Integer folderCount = allFolders.size();

        // 计算文件总大小和数量（这里需要扩展CloudFileMapper来支持按班级统计）
        // 暂时返回基础统计，后续可以优化
        return new CloudFolderStatistics(0L, 0, folderCount);
    }
}
