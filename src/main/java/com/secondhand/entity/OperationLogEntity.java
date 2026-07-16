package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLogEntity {
    private Long logId;
    private Long userId;
    private String username;
    private String operation;
    private String description;
    private String method;
    private String url;
    private String ip;
    private String userAgent;
    private String requestParams;
    private String responseData;
    private Integer duration;
    private Integer status;
    private String errorMsg;
    private LocalDateTime createTime;
}
