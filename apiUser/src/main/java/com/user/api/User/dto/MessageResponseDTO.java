package com.user.api.user.dto;

import lombok.Data;

@Data
public class MessageResponseDTO {
    private String message;

    public MessageResponseDTO(){  }

    public MessageResponseDTO(String message){
        this.message = message;
    }
}
