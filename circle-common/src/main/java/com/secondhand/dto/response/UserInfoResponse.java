package com.secondhand.dto.response;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private String avatar;
    private String role;
    private String examineState;
}