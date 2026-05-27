package com.api.Support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ApiSupportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiSupportApplication.class, args);
	}

}
