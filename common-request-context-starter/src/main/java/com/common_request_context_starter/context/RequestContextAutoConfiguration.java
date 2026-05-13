package com.common_request_context_starter.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RequestContextAutoConfiguration {
    @Bean
    public HeaderCaptureFilter headerCaptureFilter(){
        return new HeaderCaptureFilter();
    }
}
