/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CloudFolderMapper
 */
package com.tiqs.mapper;

import com.tiqs.entity.CloudFolder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CloudFolderMapper {

    @Insert("INSERT INTO cloud_folders (class_id, name, parent_folder_id, path, creator_id) " +
            "VALUES (#{classId}, #{name}, #{parentFolderId}, #{path}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CloudFolder cloudFolder);

    @Select("SELECT f.*, up.real_name as creator_name " +
            "FROM cloud_folders f " +
            "LEFT JOIN user_profile up ON f.creator_id = up.user_id " +
            "WHERE f.id = #{id}")
    CloudFolder findById(@Param("id") Long id);

    @Select("SELECT f.*, up.real_name as creator_name " +
            "FROM cloud_folders f " +
            "LEFT JOIN user_profile up ON f.creator_id = up.user_id " +
            "WHERE f.class_id = #{classId} AND f.parent_folder_id IS NULL " +
            "ORDER BY f.name")
    List<CloudFolder> findRootFolders(@Param("classId") Long classId);

    @Select("SELECT f.*, up.real_name as creator_name " +
            "FROM cloud_folders f " +
            "LEFT JOIN user_profile up ON f.creator_id = up.user_id " +
            "WHERE f.class_id = #{classId} AND f.parent_folder_id = #{parentFolderId} " +
            "ORDER BY f.name")
    List<CloudFolder> findByParentId(@Param("classId") Long classId, @Param("parentFolderId") Long parentFolderId);

    @Select("SELECT f.*, up.real_name as creator_name, " +
            "(SELECT COUNT(*) FROM cloud_folders WHERE parent_folder_id = f.id) as folder_count, " +
            "(SELECT COUNT(*) FROM cloud_files WHERE folder_id = f.id) as file_count " +
            "FROM cloud_folders f " +
            "LEFT JOIN user_profile up ON f.creator_id = up.user_id " +
            "WHERE f.class_id = #{classId} AND f.parent_folder_id = #{parentFolderId} " +
            "ORDER BY f.name")
    List<CloudFolder> findWithStats(@Param("classId") Long classId, @Param("parentFolderId") Long parentFolderId);

    @Select("SELECT COUNT(*) FROM cloud_folders WHERE class_id = #{classId} AND parent_folder_id = #{parentFolderId}")
    int countByParentId(@Param("classId") Long classId, @Param("parentFolderId") Long parentFolderId);

    @Select("SELECT COUNT(*) FROM cloud_folders WHERE class_id = #{classId} AND name = #{name} AND parent_folder_id = #{parentFolderId}")
    int countByName(@Param("classId") Long classId, @Param("name") String name, @Param("parentFolderId") Long parentFolderId);

    @Update("UPDATE cloud_folders SET name = #{name}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateName(@Param("id") Long id, @Param("name") String name);

    @Update("UPDATE cloud_folders SET parent_folder_id = #{newParentId}, path = #{newPath}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int moveFolder(@Param("id") Long id, @Param("newParentId") Long newParentId, @Param("newPath") String newPath);

    @Delete("DELETE FROM cloud_folders WHERE id = #{id}")
    int delete(@Param("id") Long id);

    @Select("WITH RECURSIVE folder_tree AS (" +
            "  SELECT id, class_id, name, parent_folder_id, path, creator_id, created_at, updated_at, 0 as level " +
            "  FROM cloud_folders WHERE id = #{folderId} " +
            "  UNION ALL " +
            "  SELECT f.id, f.class_id, f.name, f.parent_folder_id, f.path, f.creator_id, f.created_at, f.updated_at, ft.level + 1 " +
            "  FROM cloud_folders f " +
            "  INNER JOIN folder_tree ft ON f.parent_folder_id = ft.id" +
            ") " +
            "SELECT id FROM folder_tree")
    List<Long> findAllSubFolderIds(@Param("folderId") Long folderId);

    @Select("SELECT f.*, up.real_name as creator_name " +
            "FROM cloud_folders f " +
            "LEFT JOIN user_profile up ON f.creator_id = up.user_id " +
            "WHERE f.class_id = #{classId} AND f.name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY f.name")
    List<CloudFolder> searchByName(@Param("classId") Long classId, @Param("keyword") String keyword);

    @Select("SELECT f.*, up.real_name as creator_name " +
            "FROM cloud_folders f " +
            "LEFT JOIN user_profile up ON f.creator_id = up.user_id " +
            "WHERE f.class_id = #{classId} " +
            "ORDER BY f.name")
    List<CloudFolder> findAllByClassId(@Param("classId") Long classId);
}