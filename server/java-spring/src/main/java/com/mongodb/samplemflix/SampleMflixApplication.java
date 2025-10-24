package com.mongodb.samplemflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Main Spring Boot application class for the MongoDB Sample MFlix API.
 *
 * This application demonstrates MongoDB CRUD operations using the MongoDB Java Driver
 * in a Spring Boot environment. It provides a REST API for managing movie data from
 * the sample_mflix database.
 *
 * @author MongoDB Documentation Team
 * @version 1.0
 */
@SpringBootApplication
@RestController
public class SampleMflixApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleMflixApplication.class, args);
    }

    /**
     * Root endpoint providing basic information about the API.
     */
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "name", "MongoDB Sample MFlix API",
                "version", "1.0.0",
                "description", "Java Spring Boot backend demonstrating MongoDB operations with the sample_mflix dataset",
                "endpoints", Map.of("movies", "/api/movies")
        );
    }
}
