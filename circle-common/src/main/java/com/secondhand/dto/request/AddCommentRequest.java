package com.secondhand.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCommentRequest {
    private Long replyToId;

    @NotNull(message = "来源ID不能为空")
    private Long sourceId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}