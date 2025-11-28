/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description CloudFileMapper
 */
package com.tiqs.mapper;

import com.tiqs.entity.CloudFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CloudFileMapper {
    @Insert("INSERT INTO cloud_files(class_id, file_name, original_file_name, file_path, file_size, " +
            "file_type, description, uploader_id, download_count, is_public, folder_id, created_at, updated_at) " +
            "VALUES(#{classId}, #{fileName}, #{originalFileName}, #{filePath}, #{fileSize}, " +
            "#{fileType}, #{description}, #{uploaderId}, #{downloadCount}, #{isPublic}, #{folderId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CloudFile cloudFile);

    @Select("SELECT f.id, f.class_id AS classId, f.file_name AS fileName, f.original_file_name AS originalFileName, " +
            "f.file_path AS filePath, f.file_size AS fileSize, f.file_type AS fileType, f.description, " +
            "f.uploader_id AS uploaderId, f.download_count AS downloadCount, f.is_public AS isPublic, " +
            "f.created_at AS createdAt, f.updated_at AS updatedAt, " +
            "u.username AS uploaderName, u.role AS uploaderRole " +
            "FROM cloud_files f " +
            "LEFT JOIN users u ON f.uploader_id = u.id " +
            "WHERE f.id = #{id}")
    CloudFile findById(Long id);

    @Select("SELECT f.id, f.class_id AS classId, f.file_name AS fileName, f.original_file_name AS originalFileName, " +
            "f.file_path AS filePath, f.file_size AS fileSize, f.file_type AS fileType, f.description, " +
            "f.uploader_id AS uploaderId, f.download_count AS downloadCount, f.is_public AS isPublic, " +
            "f.created_at AS createdAt, f.updated_at AS updatedAt, " +
            "u.username AS uploaderName, u.role AS uploaderRole " +
            "FROM cloud_files f " +
            "LEFT JOIN users u ON f.uploader_id = u.id " +
            "WHERE f.class_id = #{classId} " +
            "ORDER BY f.created_at DESC")
    List<CloudFile> findByClassId(Long classId);

    @Select("SELECT f.id, f.class_id AS classId, f.file_name AS fileName, f.original_file_name AS originalFileName, " +
            "f.file_path AS filePath, f.file_size AS fileSize, f.file_type AS fileType, f.description, " +
            "f.uploader_id AS uploaderId, f.download_count AS downloadCount, f.is_public AS isPublic, " +
            "f.created_at AS createdAt, f.updated_at AS updatedAt, " +
            "u.username AS uploaderName, u.role AS uploaderRole " +
            "FROM cloud_files f " +
            "LEFT JOIN users u ON f.uploader_id = u.id " +
            "WHERE f.class_id = #{classId} AND f.file_type LIKE CONCAT('%', #{fileType}, '%') " +
            "ORDER BY f.created_at DESC")
    List<CloudFile> findByClassIdAndFileType(@Param("classId") Long classId, @Param("fileType") String fileType);

    @Select("SELECT f.id, f.class_id AS classId, f.file_name AS fileName, f.original_file_name AS originalFileName, " +
            "f.file_path AS filePath, f.file_size AS fileSize, f.file_type AS fileType, f.description, " +
            "f.uploader_id AS uploaderId, f.download_count AS downloadCount, f.is_public AS isPublic, " +
            "f.created_at AS createdAt, f.updated_at AS updatedAt, " +
            "u.username AS uploaderName, u.role AS uploaderRole " +
            "FROM cloud_files f " +
            "LEFT JOIN users u ON f.uploader_id = u.id " +
            "WHERE f.class_id = #{classId} AND f.uploader_id = #{uploaderId} " +
            "ORDER BY f.created_at DESC")
    List<CloudFile> findByClassIdAndUploader(@Param("classId") Long classId, @Param("uploaderId") Long uploaderId);

    @Update("UPDATE cloud_files SET description = #{description}, is_public = #{isPublic}, updated_at = NOW() WHERE id = #{id}")
    int update(CloudFile cloudFile);

    @Update("UPDATE cloud_files SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownloadCount(Long id);

    @Delete("DELETE FROM cloud_files WHERE id = #{id}")
    int delete(Long id);

    @Delete("DELETE FROM cloud_files WHERE class_id = #{classId}")
    int deleteByClassId(Long classId);

    @Select("SELECT SUM(file_size) FROM cloud_files WHERE class_id = #{classId}")
    Long getTotalSizeByClassId(Long classId);

    @Select("SELECT COUNT(*) FROM cloud_files WHERE class_id = #{classId}")
    Integer getFileCountByClassId(Long classId);

    @Select("SELECT f.id, f.class_id AS classId, f.file_name AS fileName, f.original_file_name AS originalFileName, " +
            "f.file_path AS filePath, f.file_size AS fileSize, f.file_type AS fileType, f.description, " +
            "f.uploader_id AS uploaderId, f.download_count AS downloadCount, f.is_public AS isPublic, " +
            "f.created_at AS createdAt, f.updated_at AS updatedAt, f.folder_id, " +
            "u.username AS uploaderName, u.role AS uploaderRole, " +
            "fo.name AS folderName, fo.path AS folderPath " +
            "FROM cloud_files f " +
            "LEFT JOIN users u ON f.uploader_id = u.id " +
            "LEFT JOIN cloud_folders fo ON f.folder_id = fo.id " +
            "WHERE f.folder_id = #{folderId} " +
            "ORDER BY f.created_at DESC")
    List<CloudFile> findByFolderId(@Param("folderId") Long folderId);

    @Delete("DELETE FROM cloud_files WHERE folder_id = #{folderId}")
    int deleteByFolderId(@Param("folderId") Long folderId);

    @Update("UPDATE cloud_files SET folder_id = #{folderId} WHERE id = #{fileId}")
    int moveFile(@Param("fileId") Long fileId, @Param("folderId") Long folderId);
}