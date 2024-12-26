package com.eventostec.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
		System.setProperty("AWS_JAVA_V1_DISABLE_DEPRECATION_ANNOUNCEMENT", "true");
		SpringApplication.run(ApiApplication.class, args);
	}

}
