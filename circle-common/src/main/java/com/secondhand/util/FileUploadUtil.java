package com.secondhand.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadUtil {

    @Value("${upload.path:./uploads/}")
    private String uploadPath;

    @Value("${upload.avatar.url-prefix:http://localhost:8080/uploads/avatar/}")
    private String urlPrefix;

    @Value("${upload.base-url:http://localhost:8080/uploads/}")
    private String uploadBaseUrl;

    /**
     * 上传头像
     */
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        // 1. 验证文件
        validateFile(file);

        // 2. 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + "." + extension;

        // 3. 创建目录
        Path avatarDir = Paths.get(uploadPath, "avatar");
        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }

        // 4. 保存文件
        Path filePath = avatarDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // 5. 返回访问 URL
        return urlPrefix + fileName;
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 文件大小限制 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过 5MB");
        }

        // 文件类型验证
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只能上传图片文件");
        }

        // 扩展名验证
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名无效");
        }
        String extension = getFileExtension(filename).toLowerCase();
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "webp", "svg"};
        boolean allowed = false;
        for (String ext : allowedExtensions) {
            if (ext.equals(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new RuntimeException("不支持的图片格式: " + extension);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 保存 base64 图片（商品图片）到磁盘，返回可访问 URL。
     * 若传入的不是 data:image 前缀（已是 URL 或 null），原样返回。
     */
    public String saveBase64Image(Long ownerId, String base64, String subDir) throws IOException {
        if (base64 == null || base64.isBlank() || !base64.startsWith("data:image")) {
            return base64;
        }
        int comma = base64.indexOf(',');
        if (comma < 0) {
            return base64;
        }
        String meta = base64.substring(0, comma);
        String data = base64.substring(comma + 1);

        String ext = "jpg";
        if (meta.contains("image/png")) ext = "png";
        else if (meta.contains("image/gif")) ext = "gif";
        else if (meta.contains("image/webp")) ext = "webp";
        else if (meta.contains("image/svg")) ext = "svg";

        byte[] bytes = Base64.getDecoder().decode(data);
        if (bytes.length > 5 * 1024 * 1024) {
            throw new RuntimeException("图片大小不能超过 5MB");
        }

        Path dirPath = Paths.get(uploadPath, subDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        String fileName = subDir + "_" + ownerId + "_" + System.currentTimeMillis() + "." + ext;
        Files.write(dirPath.resolve(fileName), bytes);
        return uploadBaseUrl + subDir + "/" + fileName;
    }

    /**
     * 删除旧头像
     */
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.contains("/uploads/")) {
                return false;
            }
            // 从 URL 中提取文件名
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadPath, "avatar", fileName);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }
}