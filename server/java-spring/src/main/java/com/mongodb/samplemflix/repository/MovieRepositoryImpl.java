package com.mongodb.samplemflix.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.samplemflix.model.Movie;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of MovieRepository using MongoDB Java Driver directly.
 *
 * This class demonstrates direct usage of MongoCollection<Movie> for CRUD operations.
 * It uses MongoDB's POJO codec to automatically convert between Movie objects and BSON Documents.
 *
 * This approach is used for educational purposes to show how the MongoDB driver works,
 * rather than using Spring Data MongoDB which abstracts these details.
 */
@Repository
public class MovieRepositoryImpl implements MovieRepository {

    private final MongoCollection<Movie> moviesCollection;

    public MovieRepositoryImpl(MongoDatabase mongoDatabase) {
        this.moviesCollection = mongoDatabase.getCollection("movies", Movie.class);
    }
    
    @Override
    public InsertOneResult insertOne(Movie movie) {
        return moviesCollection.insertOne(movie);
    }
    
    @Override
    public InsertManyResult insertMany(List<Movie> movies) {
        return moviesCollection.insertMany(movies);
    }
    
    @Override
    public Optional<Movie> findById(ObjectId id) {
        Movie doc = moviesCollection.find(Filters.eq(Movie.Fields.ID, id)).first();
        return Optional.ofNullable(doc);
    }
    
    @Override
    public List<Movie> find(Document filter, Document sort, int skip, int limit) {
        List<Movie> movies = new ArrayList<>();
        moviesCollection.find(filter)
                .sort(sort)
                .skip(skip)
                .limit(limit)
                .into(movies);

        return movies;
    }
    
    @Override
    public UpdateResult updateOne(ObjectId id, Document update) {
        return moviesCollection.updateOne(Filters.eq(Movie.Fields.ID, id), update);
    }
    
    @Override
    public UpdateResult updateMany(Document filter, Document update) {
        return moviesCollection.updateMany(filter, update);
    }
    
    @Override
    public DeleteResult deleteOne(ObjectId id) {
        return moviesCollection.deleteOne(Filters.eq(Movie.Fields.ID, id));
    }
    
    @Override
    public DeleteResult deleteMany(Document filter) {
        return moviesCollection.deleteMany(filter);
    }
    
    @Override
    public Optional<Movie> findOneAndDelete(ObjectId id) {
        Movie doc = moviesCollection.findOneAndDelete(Filters.eq(Movie.Fields.ID, id));
        return Optional.ofNullable(doc);
    }
    
    @Override
    public long countDocuments() {
        return moviesCollection.countDocuments();
    }

}
