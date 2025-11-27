package com.tiqs.service.impl;

import com.tiqs.auth.UserRole;
import com.tiqs.common.BusinessException;
import com.tiqs.entity.CloudFile;
import com.tiqs.mapper.CloudFileMapper;
import com.tiqs.service.CloudFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CloudFileServiceImpl implements CloudFileService {
    
    private final CloudFileMapper cloudFileMapper;
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    public CloudFileServiceImpl(CloudFileMapper cloudFileMapper) {
        this.cloudFileMapper = cloudFileMapper;
    }
    
    @Transactional
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
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
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
    
    public List<CloudFile> listByClass(Long classId) {
        log.debug("查询班级云盘文件 classId={}", classId);
        return cloudFileMapper.findByClassId(classId);
    }
    
    public List<CloudFile> listByClassAndFileType(Long classId, String fileType) {
        log.debug("按文件类型查询班级云盘文件 classId={} fileType={}", classId, fileType);
        return cloudFileMapper.findByClassIdAndFileType(classId, fileType);
    }
    
    public List<CloudFile> listByClassAndUploader(Long classId, Long uploaderId) {
        log.debug("查询用户上传的班级云盘文件 classId={} uploaderId={}", classId, uploaderId);
        return cloudFileMapper.findByClassIdAndUploader(classId, uploaderId);
    }
    
    public CloudFile get(Long id) {
        CloudFile cloudFile = cloudFileMapper.findById(id);
        if (cloudFile == null) {
            log.warn("云盘文件不存在 id={}", id);
            throw BusinessException.of(404, "文件不存在");
        }
        return cloudFile;
    }
    
    @Transactional
    public CloudFile update(CloudFile cloudFile, Long userId) {
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
    
    @Transactional
    public void delete(Long id, Long userId) {
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
    
    @Transactional(readOnly = false)
    public void incrementDownloadCount(Long id) {
        cloudFileMapper.incrementDownloadCount(id);
    }
    
    public byte[] downloadFile(Long id, Long userId) {
        CloudFile cloudFile = get(id);
        
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
    
    public CloudFileStatistics getStatistics(Long classId) {
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
            "sh", "bash", "zsh", "fish", "ps1", "py", "pl", "rb", "php",
            "asp", "jsp", "js", "vbs", "wsf", "jar", "app", "deb", "rpm",
            "dmg", "pkg", "iso", "img", "vmdk", "ova", "ovf"
        };
        
        for (String dangerousType : dangerousTypes) {
            if (extension.equals(dangerousType)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<CloudFile> listByFolder(Long folderId) {
        return cloudFileMapper.findByFolderId(folderId);
    }
    
    @Override
    @Transactional
    public CloudFile moveFile(Long fileId, Long folderId, Long userId) {
        log.info("移动文件 fileId={} folderId={} userId={}", fileId, folderId, userId);
        
        CloudFile cloudFile = get(fileId);
        cloudFileMapper.moveFile(fileId, folderId);
        cloudFile.setFolderId(folderId);
        
        return cloudFile;
    }
}