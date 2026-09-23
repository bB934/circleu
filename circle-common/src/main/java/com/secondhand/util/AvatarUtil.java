package com.secondhand.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class AvatarUtil {

    /**
     * 基于邮箱生成 Cravatar 头像 URL
     * @param email 用户邮箱
     * @param size 头像大小（像素）
     * @param defaultStyle 默认风格: identicon, monsterid, wavatar, retro, robohash
     * @return 头像 URL
     */
    public String generateCravatarAvatar(String email, int size, String defaultStyle) {
        if (email == null || email.trim().isEmpty()) {
            return generateFallbackAvatar();
        }

        String hash = md5(email.trim().toLowerCase());
        return String.format(
                "https://cravatar.cn/avatar/%s?s=%d&d=%s",
                hash,
                size,
                defaultStyle != null ? defaultStyle : "identicon"
        );
    }

    /**
     * 默认风格（identicon 几何图形）
     */
    public String generateCravatarAvatar(String email) {
        return generateCravatarAvatar(email, 200, "identicon");
    }

    /**
     * 生成降级头像（当 Cravatar 不可用时）
     */
    public String generateFallbackAvatar() {
        return "/images/default-avatar.svg";
    }

    /**
     * MD5 哈希
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * 根据用户名生成首字母头像（完全本地）
     */
    public String generateLetterAvatar(String username) {
        if (username == null || username.isEmpty()) {
            return "/images/default-avatar.svg";
        }
        // UI Avatars 服务（可选备选）
        return String.format(
                "https://ui-avatars.com/api/?name=%s&background=409eff&color=fff&size=128",
                username.substring(0, 1)
        );
    }
}