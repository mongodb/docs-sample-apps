from fastapi import APIRouter, HTTPException, Query
from src.database.mongo_client import db, get_collection
from src.models.models import CreateMovieRequest, Movie, MovieFilter, SuccessResponse
from typing import List
from datetime import datetime
from src.utils.errorHandler import create_success_response, create_error_response

'''
This file contains all the business logic for movie operations.
Each method demonstrates different MongoDB operations using the PyMongo driver.

Implemented Endpoints:
- GET /api/movies/ : Retrieve a list of movies with optional filtering, sorting,
    and pagination.
- POST /api/movies/batch : Create multiple movies in a single request.


'''
router = APIRouter()
#------------------------------------
# Place get_movie_by_id endpoint here
#------------------------------------

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
# Validate the query parameters using FastAPI's Query functionality.
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

    # This variable naming might not be ideal as Python has a function named filter, but it helps illustrate the point.
    filter = {}
    movies_collection = get_collection("movies")
    if q:
        filter["$text"] = {"$search": q}    
    if genre:
        filter["genres"] = {"$regex": genre, "$options": "i"}
    if year:
        # I personally got some dirty data in the year field, a 1995è. Should we guard against this?
        filter["year"] = year
    if min_rating is not None or max_rating is not None:
        rating_filter = {}
        if min_rating is not None:
            rating_filter["$gte"] = min_rating
        if max_rating is not None:
            rating_filter["$lte"] = max_rating
        filter["imdb.rating"] = rating_filter


    
    filter = MovieFilter(**filter)

    filter = filter.model_dump(by_alias=True, exclude_none=True)

    # Building the sort object based on user input
    sort_order = -1 if sort_order == "desc" else 1
    sort = [(sort_by, sort_order)]

    # Query the database with the constructed filter, sort, skip, and limit.
    cursor = movies_collection.find(filter).sort(sort).skip(skip_num).limit(limit_num)    
    movies = []
    async for movie in cursor:
        movie["_id"] = str(movie["_id"]) # Convert ObjectId to string
        movies.append(movie)  

    # Return the results wrapped in a SuccessResponse    
    return create_success_response(movies, f"Found {len(movies)} movies.")

#------------------------------------
# Place create_movie endpoint here
#------------------------------------

#------------------------------------
# Place create_movies_batch endpoint here
#------------------------------------

'''
POST /api/movies/batch

Create multiple movies in a single request. 

Request Body:
        movies (List[CreateMovieRequest]): A list of movie objects to insert. Each object should include:
            - title (str): The movie title.
            - year (int, optional): The release year.
            - plot (str, optional): Short plot summary.
            - fullplot (str, optional): Full plot summary.
            - genres (List[str], optional): List of genres.
            - directors (List[str], optional): List of directors.
            - writers (List[str], optional): List of writers.
            - cast (List[str], optional): List of cast members.
            - countries (List[str], optional): List of countries.
            - languages (List[str], optional): List of languages.
            - rated (str, optional): Movie rating.
            - runtime (int, optional): Runtime in minutes.
            - poster (str, optional): Poster URL.

    Returns:
        SuccessResponse: A response object containing the number of inserted movies and their IDs.

'''

@router.post("/batch")

# We do not have to write explicit validation code here, Pydantic will handle it for us.
# Invalid movies will result in a 422 response with details about the validation errors.

async def create_movies_batch(movies: List[CreateMovieRequest]):
    movies_collection = get_collection("movies")
    movies_dicts = []
    for movie in movies:
        movies_dicts.append(movie.model_dump(exclude_unset=True, exclude_none=True))

    #The above line can also be written using list comprehension as:
    # movies_dicts = [movie.model_dump(exclude_unset=True) for movie in

    result = await movies_collection.insert_many(movies_dicts)
    return create_success_response({
        "insertedCount": len(result.inserted_ids),
        "insertedIds": [str(_id) for _id in result.inserted_ids]
    },
        f"Successfully created {len(result.inserted_ids)} movies."
    )



#------------------------------------
# Place update_movie endpoint here
#------------------------------------

#------------------------------------
# Place update_movies_by_batch endpoint here
#------------------------------------

#------------------------------------
# Place delete_movie endpoint here
#------------------------------------

#------------------------------------
# Place delete_movies_by_batch endpoint here
#------------------------------------

#------------------------------------
# Place find_and_delete_movie endpoint here
#------------------------------------


# ---- Old testing endpoint, will be removed later ----
'''
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
'''