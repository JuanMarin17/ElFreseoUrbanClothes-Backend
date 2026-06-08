package com.api.product.controller;

import com.common_request_context_starter.context.RequestContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> headers() {
        return ResponseEntity.ok(RequestContext.getAllHeaders());
    }
}
