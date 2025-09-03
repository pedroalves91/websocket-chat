package com.vinturas.chatcourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ChatcourseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatcourseApplication.class, args);
	}

}
