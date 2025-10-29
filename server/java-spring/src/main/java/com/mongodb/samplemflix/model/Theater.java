package com.mongodb.samplemflix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain model representing a theater document from the MongoDB theaters collection.
 *
 * <p>This class maps to the theaters collection in the sample_mflix database.
 * It includes location information with address and geospatial coordinates.
 *
 * <p>TODO: Implement Theater functionality:
 * - Create TheaterRepository extending MongoRepository
 * - Create TheaterService and TheaterServiceImpl
 * - Create TheaterController with REST endpoints
 * - Implement geospatial queries (findNear, findWithinRadius)
 * - Add validation annotations
 * - Add unit tests for Theater service and controller
 * - Add integration tests with geospatial queries
 * - Add GeoJSON support for location queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "theaters")
public class Theater {
    
    /**
     * MongoDB document ID.
     * Maps to the _id field in MongoDB.
     */
    @Id
    private ObjectId id;
    
    /**
     * Theater ID number.
     */
    private Integer theaterId;
    
    /**
     * Location information including address and geospatial coordinates.
     */
    private Location location;
    
    /**
     * Nested class representing location information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        /**
         * Address information.
         */
        private Address address;
        
        /**
         * Geospatial coordinates.
         */
        private Geo geo;
        
        /**
         * Nested class for address information.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Address {
            /**
             * Street address line 1.
             */
            private String street1;
            
            /**
             * City name.
             */
            private String city;
            
            /**
             * State or province.
             */
            private String state;
            
            /**
             * ZIP or postal code.
             */
            private String zipcode;
        }
        
        /**
         * Nested class for geospatial coordinates.
         * Uses GeoJSON format for MongoDB geospatial queries.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Geo {
            /**
             * GeoJSON type (always "Point" for theater locations).
             */
            private String type;
            
            /**
             * Coordinates array: [longitude, latitude].
             * Note: GeoJSON uses longitude first, then latitude.
             */
            private double[] coordinates;
        }
    }
}
