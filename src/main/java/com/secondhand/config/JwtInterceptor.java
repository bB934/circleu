package com.secondhand.config;

import com.secondhand.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    // ✅ 放行路径列表（免 token，但带 token 时仍解析写入 userId 以支持收藏态等）
    // 注意：必须排除 /goods/favorites、/goods/{id}/favorite 等需登录态的接口
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/uploads/",
            "/user/login",
            "/user/register",
            "/goods/list",
            "/goods/categories",
            "/goods/",
            "/announcements",
            "/carousels",
            "/news/list",
            "/news/"
    );

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 需登录态的商品子路径（收藏）不被白名单误放行
        boolean isGoodsAuthPath = path.startsWith("/goods/")
                && (path.endsWith("/favorites") || path.endsWith("/favorite"));

        // ✅ 检查是否在白名单中（免 token）
        boolean inWhiteList = !isGoodsAuthPath
                && WHITE_LIST.stream().anyMatch(path::startsWith);

        // JWT 验证（非白名单必须带合法 token）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (inWhiteList) {
                // 白名单：无 token 直接放行（游客访问）
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\"}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            if (inWhiteList) {
                // 白名单：token 无效也放行（游客访问），不写 userId
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        return true;
    }

}