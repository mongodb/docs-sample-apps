package com.mongodb.samplemflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
public class SampleMflixApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleMflixApplication.class, args);
    }
}

