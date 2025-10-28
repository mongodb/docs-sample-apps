from fastapi import FastAPI
from src.routers import movies
from src.utils.errorHandler import register_error_handlers
from src.database.mongo_client import db

app = FastAPI()
register_error_handlers(app)
app.include_router(movies.router, prefix="/api/movies", tags=["movies"])


@app.on_event("startup")
async def ensure_search_index():
    try:
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
        # This will create or update the Atlas Search index
        await db.command({
            "createSearchIndexes": "movies",
            "indexes": [{
                "name": "movieSearchIndex",
                "definition": index_definition
            }]
        })
        print("Atlas Search index ensured.")
    except Exception as e:
        print(f"Error creating the search index: {e}")



