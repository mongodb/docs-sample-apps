package com.mongodb.samplemflix.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for batch update operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateResponse {
    private long matchedCount;
    private long modifiedCount;
}

