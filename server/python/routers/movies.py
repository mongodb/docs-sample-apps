from fastapi import APIRouter, HTTPException, Query
from database.mongo_client import db
from models.models import Movie, MovieFilter, SuccessResponse
from typing import List
from datetime import datetime
from utils.errorHandler import create_success_response, create_error_response

router = APIRouter()


"""
    GET /api/movies/

    Retrieve a list of movies with optional filtering, sorting, and pagination.

    Query Parameters:
        q (str, optional): Text search query (searches title, plot, fullplot).
        genre (str, optional): Filter by genre.
        year (int, optional): Filter by year.
        min_rating (float, optional): Minimum IMDB rating.
        max_rating (float, optional): Maximum IMDB rating.
        limitNum (int, optional): Number of results to return (default: 20, max: 100).
        skipNum (int, optional): Number of documents to skip for pagination (default: 0).
        sortBy (str, optional): Field to sort by (default: "title").
        sort_order (str, optional): Sort direction, "asc" or "desc" (default: "asc").

    Returns:
        SuccessResponse[List[Movie]]: A response object containing the list of movies and metadata.
"""

@router.get("/", response_model=SuccessResponse[List[Movie]])
async def get_all_movies(
    q:str = Query(default=None),
    genre:str = Query(default=None),
    year:int = Query(default=None),
    min_rating:float = Query(default=None),
    max_rating:float = Query(default=None),
    limit_num:int = Query(default=20, ge=1, le=100),
    skip_num:int = Query(default=0, ge=0),
    sort_by:str = Query(default="title"),
    sort_order:str = Query(default="asc")
):

    # This variable naming might not be ideal, but it helps illustrate the point.
    filter = {}
    if q:
        filter["$text"] = {"$search": q}    
    if genre:
        filter["genres"] = {"$regex": genre, "$options": "i"}
    if year:
        # I personally got some dirty data in the year field, a 1995è
        filter["year"] = year
    if min_rating is not None or max_rating is not None:
        rating_filter = {}
        if min_rating is not None:
            rating_filter["$gte"] = min_rating
        if max_rating is not None:
            rating_filter["$lte"] = max_rating
        filter["imdb.rating"] = rating_filter


    # Comment why this is important
    filter = MovieFilter(**filter)

    filter = filter.model_dump(by_alias=True, exclude_none=True)

    sort_order = -1 if sort_order == "desc" else 1
    sort = [(sort_by, sort_order)]

    
    cursor = db.movies.find(filter).sort(sort).skip(skip_num).limit(limit_num)    
    movies = []
    async for movie in cursor:
        movie["_id"] = str(movie["_id"]) # Convert ObjectId to string
        movies.append(movie)  
    return create_success_response(movies, f"Found {len(movies)} movies.")






# Testing the ErrorReponse Model
@router.get("/error")
async def test_error():
    try:
        raise ValueError("This is a test error.")
    except ValueError as e:
        return create_error_response(
                message="A test error occurred.",
                code="TEST_ERROR",
                details=str(e)
            )