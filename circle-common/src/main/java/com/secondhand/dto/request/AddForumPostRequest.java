package com.secondhand.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddForumPostRequest {
    @NotBlank(message = "帖子标题不能为空")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    private String content;

    private String category;
}