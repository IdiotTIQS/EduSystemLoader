package com.tiqs.service.impl;

import com.tiqs.common.BusinessException;
import com.tiqs.entity.CloudFolder;
import com.tiqs.mapper.CloudFileMapper;
import com.tiqs.mapper.CloudFolderMapper;
import com.tiqs.service.CloudFolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CloudFolderServiceImpl implements CloudFolderService {

    @Autowired
    private CloudFolderMapper cloudFolderMapper;

    @Autowired
    private CloudFileMapper cloudFileMapper;

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
    public CloudFolderStatistics getStatistics(Long classId) {
        // 获取所有文件夹
        List<CloudFolder> allFolders = cloudFolderMapper.findAllByClassId(classId);
        Integer folderCount = allFolders.size();

        // 计算文件总大小和数量（这里需要扩展CloudFileMapper来支持按班级统计）
        // 暂时返回基础统计，后续可以优化
        return new CloudFolderStatistics(0L, 0, folderCount);
    }
}