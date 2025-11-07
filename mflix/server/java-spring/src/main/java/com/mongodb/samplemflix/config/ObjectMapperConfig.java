package com.mongodb.samplemflix.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

/**
 * Configuration for customizing the ObjectMapper used for JSON serialization and deserialization.
 *
 * <p>This configuration disables the default timestamp serialization for dates and registers a
 * custom serializer for MongoDB's ObjectId to convert it to a string representation.
 *
 * <p>It also registers a JavaTimeModule to handle Java 8 date and time types.
 */

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper(JsonFactory jsonFactory) {
        ObjectMapper mapper =
                new ObjectMapper(jsonFactory)
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Bean
    public JsonFactory jsonFactory() {
        return new JsonFactory();
    }

    @Bean
    public DataBufferFactory dataBufferFactory() {
        return new DefaultDataBufferFactory();
    }
}
