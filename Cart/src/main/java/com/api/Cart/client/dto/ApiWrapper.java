package com.api.Cart.client.dto;

import lombok.Data;

@Data
public class ApiWrapper<T> {
    private T data;
}
