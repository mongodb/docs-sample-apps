package com.mongodb.samplemflix.repository;

/**
 * Repository interface for movie data access.
 * 
 * This repository provides methods for all CRUD operations using the MongoDB Java Driver.
 * 
 * Methods to implement:
 * - insertOne(Movie movie)
 * - insertMany(List<Movie> movies)
 * - findById(ObjectId id)
 * - find(Document filter, Document sort, int skip, int limit)
 * - updateOne(ObjectId id, Document update)
 * - updateMany(Document filter, Document update)
 * - deleteOne(ObjectId id)
 * - deleteMany(Document filter)
 * - findOneAndDelete(ObjectId id)
 * - countDocuments()
 * 
 * TODO: Phase 4 - Define repository interface methods
 * TODO: Phase 4 - Create implementation class using MongoCollection
 */
public interface MovieRepository {
    
    // TODO: Phase 4 - Add method signatures
}

