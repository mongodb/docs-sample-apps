package com.mongodb.samplemflix.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.BsonValue;

import java.util.Collection;

/**
 * Response DTO for batch insert operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchInsertResponse {
    private int insertedCount;
    private Collection<BsonValue> insertedIds;
}

