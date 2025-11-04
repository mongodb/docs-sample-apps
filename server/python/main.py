from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.routers import movies
from src.utils.errorHandler import register_error_handlers
from src.database.mongo_client import db
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
async def ensure_search_index():
    try:
        movies_collection = db.get_collection("movies")
        result = await movies_collection.list_search_indexes()
        indexes = [idx async for idx in result]
        index_names = [index["name"] for index in indexes]
        if "movieSearchIndex" in index_names:
            print("MongoDB Search index already exists.")
            return

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
    except Exception as e:
        print(f"Error creating the search index: {e}")




