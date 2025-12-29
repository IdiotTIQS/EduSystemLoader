/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description FileUploadController
 */
package com.tiqs.controller;

import com.tiqs.auth.AuthContext;
import com.tiqs.auth.AuthContextHolder;
import com.tiqs.auth.AuthException;
import com.tiqs.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/file")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        requireAuth();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > 100 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小超出限制，请选择小于100MB的文件");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

                // 禁止的可执行文件和SQL文件扩展名
                String[] forbiddenExtensions = {
                        ".exe", ".bat", ".sh", ".cmd", ".com", ".scr", ".msi",
                        ".sql", ".ddl", ".dml", ".pl", ".php", ".asp", ".jsp",
                        ".js", ".vbs", ".py", ".rb", ".ps1", ".bash", ".zsh",
                        ".html", ".htm", ".svg", ".xml", ".xhtml"
                };

                for (String ext : forbiddenExtensions) {
                    if (fileExtension.equals(ext)) {
                        throw new IllegalArgumentException("不支持上传该类型的文件: " + fileExtension);
                    }
                }
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一的文件名
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID() + fileExtension;

            // 按日期创建子目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dateDir = uploadPath.resolve(datePath);
            if (!Files.exists(dateDir)) {
                Files.createDirectories(dateDir);
            }

            // 保存文件
            Path targetLocation = dateDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 返回文件的访问路径
            String fileUrl = "/uploads/" + datePath + "/" + uniqueFilename;
            return ApiResponse.ok(fileUrl);

        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    private void requireAuth() {
        AuthContext auth = AuthContextHolder.get();
        if (auth == null) {
            throw new AuthException("未登录或登录已失效");
        }
    }
}