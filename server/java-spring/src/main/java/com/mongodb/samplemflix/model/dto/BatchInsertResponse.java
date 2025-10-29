package com.mongodb.samplemflix.model.dto;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.BsonValue;

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

