package com.api.Returns.dto;

import com.api.Returns.enums.ReturnStatus;

import lombok.Data;

@Data
public class UpdateReturnStatusDTO {
    private ReturnStatus status;
}