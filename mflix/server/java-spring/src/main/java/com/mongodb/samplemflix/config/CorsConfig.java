package com.mongodb.samplemflix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the Sample MFlix API.
 *
 * <p>This configuration allows the frontend application (typically running on a different port
 * during development) to make requests to this backend API.
 *
 * <p>The allowed origins are configured via the CORS_ORIGIN environment variable.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    /**
     * Configures CORS filter to allow cross-origin requests from the frontend.
     * 
     * @return configured CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow wildcard origins for Swagger UI
        config.setAllowCredentials(false);

        // Set allowed origins - include localhost for Swagger UI
        // Parse the configured origins and add localhost variations for Swagger UI
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            config.addAllowedOrigin(origin.trim());
        }
        // Add localhost origins for Swagger UI (on any port)
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all HTTP methods
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
