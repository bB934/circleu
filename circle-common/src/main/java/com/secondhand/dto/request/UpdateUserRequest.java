package com.secondhand.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String phone;
    private String gender;
    private String avatar;
}