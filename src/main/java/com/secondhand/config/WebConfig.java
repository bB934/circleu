package com.secondhand.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    private static final List<String> EXCLUDE_PATHS = List.of(
            "/user/login",
            "/user/register",
            "/announcements",
            "/carousels",
            "/news/list",
            "/news/**",
            "/goods/list",
            "/goods/categories",
            "/goods/*/favorite",
            "/forum_posts",
            "/error"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/Users/Zhuanz1/Desktop/spring-boot/demo/uploads/");
        // 注意：路径需要是绝对路径，必须以 file: 开头
    }
}