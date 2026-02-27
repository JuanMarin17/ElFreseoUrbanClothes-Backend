package com.user.api.User.dto;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String lastName;
    private String email;
    private String password;
    private String phone;
}
