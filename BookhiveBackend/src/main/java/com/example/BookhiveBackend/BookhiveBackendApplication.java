package com.example.BookhiveBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BookhiveBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BookhiveBackendApplication.class, args);
	}
}