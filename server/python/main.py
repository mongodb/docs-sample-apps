from fastapi import FastAPI
from src.routers import movies
from src.utils.errorHandler import register_error_handlers

app = FastAPI()
register_error_handlers(app)
app.include_router(movies.router, prefix="/api/movies", tags=["movies"])








#------------------------------------
# Testing error endpoints. Will be removed later
#------------------------------------

'''
@app.get("/")
async def root():
    return {"message": "Backend is running!"}

@app.get("/test-duplicate")
async def test_duplicate():
    from pymongo.errors import DuplicateKeyError
    raise DuplicateKeyError("This is a test duplicate key error.")

@app.get("/test-generic")
async def test_generic():
    from pymongo.errors import PyMongoError
    raise PyMongoError("This is a test generic pymongo error.")
'''