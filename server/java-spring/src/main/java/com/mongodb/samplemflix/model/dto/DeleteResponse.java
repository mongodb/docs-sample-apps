package com.mongodb.samplemflix.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for delete operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteResponse {
    private long deletedCount;
}

