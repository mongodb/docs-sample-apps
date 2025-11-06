from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.routers import movies
from src.utils.errorHandler import register_error_handlers
from src.database.mongo_client import db, get_collection
import traceback
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

app = FastAPI()

# Add CORS middleware
cors_origins = os.getenv("CORS_ORIGINS", "http://localhost:3000,http://localhost:3001").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=[origin.strip() for origin in cors_origins],  # Load from environment variable
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

register_error_handlers(app)
app.include_router(movies.router, prefix="/api/movies", tags=["movies"])


@app.on_event("startup")
async def initialize_database_indexes():
    try:
        movies_collection = db.get_collection("movies")
        comments_collection = db.get_collection("comments")
        
        # Check and create search index for movies collection
        result = await movies_collection.list_search_indexes()
        indexes = [idx async for idx in result]
        index_names = [index["name"] for index in indexes]
        if "movieSearchIndex" in index_names:
            print("MongoDB Search index already exists.")
        else:
            # Create a mapping if the movieSearchIndex does not exist
            index_definition = {
                "mappings": {
                    "dynamic": False,
                    "fields": {
                        "plot": {"type": "string", "analyzer": "lucene.standard"},
                        "fullplot": {"type": "string", "analyzer": "lucene.standard"},
                        "directors": {"type": "string", "analyzer": "lucene.standard"},
                        "writers": {"type": "string", "analyzer": "lucene.standard"},
                        "cast": {"type": "string", "analyzer": "lucene.standard"}
                    }
                }
            }
            # Creates movieSearchIndex on the movies collection
            await db.command({
                "createSearchIndexes": "movies",
                "indexes": [{
                    "name": "movieSearchIndex",
                    "definition": index_definition
                }]
            })
            print("MongoDB Search index created.")
        
        # Check and create index on movie_id field in comments collection
        # This index will significantly improve $lookup performance in aggregations
        cursor = await comments_collection.list_indexes()
        existing_indexes = await cursor.to_list(length=None)
        movie_id_index_exists = any(
            "movie_id" in index.get("key", {}) for index in existing_indexes
        )
        
        if not movie_id_index_exists:
            # Create index on movie_id field for better aggregation performance
            await comments_collection.create_index("movie_id")
            print("Index on 'movie_id' field in comments collection created.")
        else:
            print("Index on 'movie_id' field in comments collection already exists.")
            
        # Also create a compound index on movie_id and date for even better performance
        # when sorting comments by date within each movie
        compound_index_exists = any(
            index.get("key", {}).get("movie_id") == 1 and index.get("key", {}).get("date") == -1
            for index in existing_indexes
        )
        
        if not compound_index_exists:
            await comments_collection.create_index([("movie_id", 1), ("date", -1)])
            print("Compound index on 'movie_id' and 'date' fields in comments collection created.")
        else:
            print("Compound index on 'movie_id' and 'date' fields already exists.")
            
    except Exception as e:
        print(f"Error creating indexes: {e}")

@app.on_event("startup")
async def vector_search_index():
    """
    Creates vector search index on application startup if it doesn't already exist.
    This ensures the index is ready before any vector search requests are made.
    """
    try:
        
        embedded_movies_collection = get_collection("embedded_movies")
        
        # Get list of existing indexes - convert AsyncCommandCursor to list
        existing_indexes_cursor = await embedded_movies_collection.list_search_indexes()
        existing_indexes = await existing_indexes_cursor.to_list(length=None)
        index_names = [index.get("name") for index in existing_indexes]
        
        # Check if our vector_index already exists
        if "vector_index" not in index_names:
            
            # Define the vector search index specification
            index_definition = {
                "name": "vector_index",
                "type": "vectorSearch",
                "definition": {
                    "fields": [
                        {
                            "type": "vector",
                            "path": "plot_embedding_voyage_3_large",
                            "numDimensions": 2048, #Set this to 2048 to match the embedding dimensions on the path
                            "similarity": "cosine"
                        }
                    ]
                }
            }
            
            # Create the index
            result = await embedded_movies_collection.create_search_index(index_definition)
            print("Vector search index 'vector_index' ready to query.")
            
    except Exception as e:
        print(f"Error during vector search index setup: {str(e)}")
        print(f"Error type: {type(e).__name__}")

