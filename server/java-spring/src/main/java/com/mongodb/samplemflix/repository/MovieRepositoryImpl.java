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
import java.util.stream.Collectors;

/**
 * Implementation of MovieRepository using MongoDB Java Driver directly.
 * 
 * This class demonstrates direct usage of MongoCollection<Document> for CRUD operations.
 * It manually converts between Movie objects and BSON Documents to provide full control
 * over the MongoDB operations.
 * 
 * This approach is used for educational purposes to show how the MongoDB driver works,
 * rather than using Spring Data MongoDB which abstracts these details.
 */
@Repository
public class MovieRepositoryImpl implements MovieRepository {
    
    private final MongoCollection<Document> moviesCollection;
    
    public MovieRepositoryImpl(MongoDatabase mongoDatabase) {
        this.moviesCollection = mongoDatabase.getCollection("movies");
    }
    
    @Override
    public InsertOneResult insertOne(Movie movie) {
        Document doc = movieToDocument(movie);
        return moviesCollection.insertOne(doc);
    }
    
    @Override
    public InsertManyResult insertMany(List<Movie> movies) {
        List<Document> documents = movies.stream()
                .map(this::movieToDocument)
                .collect(Collectors.toList());
        return moviesCollection.insertMany(documents);
    }
    
    @Override
    public Optional<Movie> findById(ObjectId id) {
        Document doc = moviesCollection.find(Filters.eq("_id", id)).first();
        return Optional.ofNullable(doc).map(this::documentToMovie);
    }
    
    @Override
    public List<Movie> find(Document filter, Document sort, int skip, int limit) {
        List<Movie> movies = new ArrayList<>();
        moviesCollection.find(filter)
                .sort(sort)
                .skip(skip)
                .limit(limit)
                .forEach(doc -> movies.add(documentToMovie(doc)));
        return movies;
    }
    
    @Override
    public UpdateResult updateOne(ObjectId id, Document update) {
        return moviesCollection.updateOne(Filters.eq("_id", id), update);
    }
    
    @Override
    public UpdateResult updateMany(Document filter, Document update) {
        return moviesCollection.updateMany(filter, update);
    }
    
    @Override
    public DeleteResult deleteOne(ObjectId id) {
        return moviesCollection.deleteOne(Filters.eq("_id", id));
    }
    
    @Override
    public DeleteResult deleteMany(Document filter) {
        return moviesCollection.deleteMany(filter);
    }
    
    @Override
    public Optional<Movie> findOneAndDelete(ObjectId id) {
        Document doc = moviesCollection.findOneAndDelete(Filters.eq("_id", id));
        return Optional.ofNullable(doc).map(this::documentToMovie);
    }
    
    @Override
    public long countDocuments() {
        return moviesCollection.countDocuments();
    }
    
    /**
     * Converts a Movie object to a BSON Document.
     * 
     * @param movie the Movie object
     * @return the BSON Document
     */
    private Document movieToDocument(Movie movie) {
        Document doc = new Document();
        
        if (movie.getId() != null) {
            doc.append("_id", movie.getId());
        }
        if (movie.getTitle() != null) {
            doc.append("title", movie.getTitle());
        }
        if (movie.getYear() != null) {
            doc.append("year", movie.getYear());
        }
        if (movie.getPlot() != null) {
            doc.append("plot", movie.getPlot());
        }
        if (movie.getFullplot() != null) {
            doc.append("fullplot", movie.getFullplot());
        }
        if (movie.getReleased() != null) {
            doc.append("released", movie.getReleased());
        }
        if (movie.getRuntime() != null) {
            doc.append("runtime", movie.getRuntime());
        }
        if (movie.getPoster() != null) {
            doc.append("poster", movie.getPoster());
        }
        if (movie.getGenres() != null) {
            doc.append("genres", movie.getGenres());
        }
        if (movie.getDirectors() != null) {
            doc.append("directors", movie.getDirectors());
        }
        if (movie.getWriters() != null) {
            doc.append("writers", movie.getWriters());
        }
        if (movie.getCast() != null) {
            doc.append("cast", movie.getCast());
        }
        if (movie.getCountries() != null) {
            doc.append("countries", movie.getCountries());
        }
        if (movie.getLanguages() != null) {
            doc.append("languages", movie.getLanguages());
        }
        if (movie.getRated() != null) {
            doc.append("rated", movie.getRated());
        }
        if (movie.getAwards() != null) {
            doc.append("awards", awardsToDocument(movie.getAwards()));
        }
        if (movie.getImdb() != null) {
            doc.append("imdb", imdbToDocument(movie.getImdb()));
        }
        if (movie.getTomatoes() != null) {
            doc.append("tomatoes", tomatoesToDocument(movie.getTomatoes()));
        }
        if (movie.getMetacritic() != null) {
            doc.append("metacritic", movie.getMetacritic());
        }
        if (movie.getType() != null) {
            doc.append("type", movie.getType());
        }
        
        return doc;
    }
    
    /**
     * Converts a BSON Document to a Movie object.
     * 
     * @param doc the BSON Document
     * @return the Movie object
     */
    @SuppressWarnings("unchecked")
    private Movie documentToMovie(Document doc) {
        Movie movie = new Movie();
        
        movie.setId(doc.getObjectId("_id"));
        movie.setTitle(doc.getString("title"));
        movie.setYear(doc.getInteger("year"));
        movie.setPlot(doc.getString("plot"));
        movie.setFullplot(doc.getString("fullplot"));
        movie.setReleased(doc.getDate("released"));
        movie.setRuntime(doc.getInteger("runtime"));
        movie.setPoster(doc.getString("poster"));
        movie.setGenres((List<String>) doc.get("genres"));
        movie.setDirectors((List<String>) doc.get("directors"));
        movie.setWriters((List<String>) doc.get("writers"));
        movie.setCast((List<String>) doc.get("cast"));
        movie.setCountries((List<String>) doc.get("countries"));
        movie.setLanguages((List<String>) doc.get("languages"));
        movie.setRated(doc.getString("rated"));
        movie.setMetacritic(doc.getInteger("metacritic"));
        movie.setType(doc.getString("type"));
        
        Document awardsDoc = (Document) doc.get("awards");
        if (awardsDoc != null) {
            movie.setAwards(documentToAwards(awardsDoc));
        }
        
        Document imdbDoc = (Document) doc.get("imdb");
        if (imdbDoc != null) {
            movie.setImdb(documentToImdb(imdbDoc));
        }
        
        Document tomatoesDoc = (Document) doc.get("tomatoes");
        if (tomatoesDoc != null) {
            movie.setTomatoes(documentToTomatoes(tomatoesDoc));
        }
        
        return movie;
    }
    
    private Document awardsToDocument(Movie.Awards awards) {
        Document doc = new Document();
        if (awards.getWins() != null) {
            doc.append("wins", awards.getWins());
        }
        if (awards.getNominations() != null) {
            doc.append("nominations", awards.getNominations());
        }
        if (awards.getText() != null) {
            doc.append("text", awards.getText());
        }
        return doc;
    }
    
    private Movie.Awards documentToAwards(Document doc) {
        return Movie.Awards.builder()
                .wins(doc.getInteger("wins"))
                .nominations(doc.getInteger("nominations"))
                .text(doc.getString("text"))
                .build();
    }
    
    private Document imdbToDocument(Movie.Imdb imdb) {
        Document doc = new Document();
        if (imdb.getRating() != null) {
            doc.append("rating", imdb.getRating());
        }
        if (imdb.getVotes() != null) {
            doc.append("votes", imdb.getVotes());
        }
        if (imdb.getId() != null) {
            doc.append("id", imdb.getId());
        }
        return doc;
    }
    
    private Movie.Imdb documentToImdb(Document doc) {
        return Movie.Imdb.builder()
                .rating(doc.getDouble("rating"))
                .votes(doc.getInteger("votes"))
                .id(doc.getInteger("id"))
                .build();
    }

    private Document tomatoesToDocument(Movie.Tomatoes tomatoes) {
        Document doc = new Document();
        if (tomatoes.getViewer() != null) {
            doc.append("viewer", viewerToDocument(tomatoes.getViewer()));
        }
        if (tomatoes.getCritic() != null) {
            doc.append("critic", criticToDocument(tomatoes.getCritic()));
        }
        if (tomatoes.getFresh() != null) {
            doc.append("fresh", tomatoes.getFresh());
        }
        if (tomatoes.getRotten() != null) {
            doc.append("rotten", tomatoes.getRotten());
        }
        if (tomatoes.getProduction() != null) {
            doc.append("production", tomatoes.getProduction());
        }
        if (tomatoes.getLastUpdated() != null) {
            doc.append("lastUpdated", tomatoes.getLastUpdated());
        }
        return doc;
    }

    private Movie.Tomatoes documentToTomatoes(Document doc) {
        Movie.Tomatoes.Viewer viewer = null;
        Document viewerDoc = (Document) doc.get("viewer");
        if (viewerDoc != null) {
            viewer = documentToViewer(viewerDoc);
        }

        Movie.Tomatoes.Critic critic = null;
        Document criticDoc = (Document) doc.get("critic");
        if (criticDoc != null) {
            critic = documentToCritic(criticDoc);
        }

        return Movie.Tomatoes.builder()
                .viewer(viewer)
                .critic(critic)
                .fresh(doc.getInteger("fresh"))
                .rotten(doc.getInteger("rotten"))
                .production(doc.getString("production"))
                .lastUpdated(doc.getDate("lastUpdated"))
                .build();
    }

    private Document viewerToDocument(Movie.Tomatoes.Viewer viewer) {
        Document doc = new Document();
        if (viewer.getRating() != null) {
            doc.append("rating", viewer.getRating());
        }
        if (viewer.getNumReviews() != null) {
            doc.append("numReviews", viewer.getNumReviews());
        }
        if (viewer.getMeter() != null) {
            doc.append("meter", viewer.getMeter());
        }
        return doc;
    }

    private Movie.Tomatoes.Viewer documentToViewer(Document doc) {
        return Movie.Tomatoes.Viewer.builder()
                .rating(doc.getDouble("rating"))
                .numReviews(doc.getInteger("numReviews"))
                .meter(doc.getInteger("meter"))
                .build();
    }

    private Document criticToDocument(Movie.Tomatoes.Critic critic) {
        Document doc = new Document();
        if (critic.getRating() != null) {
            doc.append("rating", critic.getRating());
        }
        if (critic.getNumReviews() != null) {
            doc.append("numReviews", critic.getNumReviews());
        }
        if (critic.getMeter() != null) {
            doc.append("meter", critic.getMeter());
        }
        return doc;
    }

    private Movie.Tomatoes.Critic documentToCritic(Document doc) {
        return Movie.Tomatoes.Critic.builder()
                .rating(doc.getDouble("rating"))
                .numReviews(doc.getInteger("numReviews"))
                .meter(doc.getInteger("meter"))
                .build();
    }
}
