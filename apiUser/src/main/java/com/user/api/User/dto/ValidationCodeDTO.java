package com.user.api.user.dto;

import lombok.Data;

@Data
public class ValidationCodeDTO {
    private String email;
    private Integer code;
}
