from fastapi import APIRouter, Query, Path
from src.database.mongo_client import db, get_collection
from src.models.models import CreateMovieRequest, Movie, MovieFilter, SuccessResponse, UpdateMovieRequest
from typing import List
from datetime import datetime
from src.utils.errorHandler import create_success_response, create_error_response
from bson import ObjectId
import re

'''
This file contains all the business logic for movie operations.
Each method demonstrates different MongoDB operations using the PyMongo driver.

Implemented Endpoints:
- GET /api/movies/ : Retrieve a list of movies with optional filter, sorting,
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
    title: str = Query(default=None),
    genre:str = Query(default=None),
    year:int = Query(default=None),
    min_rating:float = Query(default=None),
    max_rating:float = Query(default=None),
    limit:int = Query(default=20, ge=1, le=100),
    skip:int = Query(default=0, ge=0),
    sort_by:str = Query(default="title"),
    sort_order:str = Query(default="asc")
):
    movies_collection = get_collection("movies")
    filter_dict = {}
    if q:
        filter_dict["$text"] = {"$search": q} 
    if title:
        filter_dict["title"] = {"$regex": title, "$options": "i"}       
    if genre:
        filter_dict["genres"] = {"$regex": genre, "$options": "i"}
    if year:
        filter_dict["year"] = year
    if min_rating is not None or max_rating is not None:
        rating_filter = {}
        if min_rating is not None:
            rating_filter["$gte"] = min_rating
        if max_rating is not None:
            rating_filter["$lte"] = max_rating
        filter_dict["imdb.rating"] = rating_filter
        
    # Building the sort object based on user input
    sort_order = -1 if sort_order == "desc" else 1

    sort = [(sort_by, sort_order)]

    # Query the database with the constructed filter, sort, skip, and limit.
    result = movies_collection.find(filter_dict).sort(sort).skip(skip).limit(limit)  

    movies = []

    async for movie in result:
        movie["_id"] = str(movie["_id"]) # Convert ObjectId to string
        # Ensure that the year field contains int value.
        if "year" in movie and not isinstance(movie["year"], int):
            cleaned_year = re.sub(r"\D", "", str(movie["year"]))
            try:
                movie["year"] = int(cleaned_year) if cleaned_year else None
            except ValueError:
                movie["year"] = None

        movies.append(movie)            

    # Return the results wrapped in a SuccessResponse    
    return create_success_response(movies, f"Found {len(movies)} movies.")

#------------------------------------
# Place create_movie endpoint here
#------------------------------------

#------------------------------------
# Place create_movies_batch endpoint here
#------------------------------------

"""
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

"""

@router.post("/batch")
async def create_movies_batch(movies: List[CreateMovieRequest]):
    movies_collection = get_collection("movies")

    #Verify that the movies list is not empty
    if not movies:
        return create_error_response(
            message="Request body must be a non-empty list of movies.",
            code="INVALID_INPUT",
            details=None
        )
    
    movies_dicts = []

    for movie in movies:
        movies_dicts.append(movie.model_dump(exclude_unset=True, exclude_none=True))
    
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

"""    
    PATCH /api/movies/{movie_id}

    Update a single movie by its ID.

    Path Parameters:
        movie_id (str): The ObjectId of the movie to update

    Request Body:
        move_data (UpdateMovieRequest): Fields and values to update. Only provided fields will be updated.

    Returns:
        SuccessResponse: The updated movie document, the number of fields modified and a success message.
"""
@router.patch("/{movie_id}")
async def update_movie(
    movie_data: UpdateMovieRequest,
    movie_id: str = Path(...),
    
):

    movies_collection = get_collection("movies")
    
    # Validate the ObjectId
    try:
        movie_id = ObjectId(movie_id)
    except:
        return create_error_response(
            message="Invalid movie_id format.",
            code="INVALID_OBJECT_ID",
            details=str(movie_id)
        )    
    
    update_dict = movie_data.model_dump(exclude_unset=True, exclude_none=True)

    # Validate that the dict is not empty
    if not update_dict:
        return create_error_response(
            message="No valid fields provided for update.",
            code="NO_UPDATE_DATA",
            details=None
        )

    result = await movies_collection.update_one(
        {"_id": movie_id},
        {"$set":update_dict}
    )

    if result.matched_count == 0:
        return create_error_response(
            message="No movie with that _id was found.",
            code="MOVIE_NOT_FOUND",
            details=str(movie_id)

        )
    
    updatedMovie = await movies_collection.find_one({"_id": str(movie_id)})

    return create_success_response(updatedMovie, f"Movie updated successfully. Modified {len(update_dict)} fields.")




#------------------------------------
# Place update_movies_by_batch endpoint here
#------------------------------------


"""
    PATCH /api/movies

    Batch update movies matching the given filter

    Request Body:
        filter (MoviesUpdateFilter): Criteria to select which movies to update. Only movies matching this filter will be updated.
        update (UpdateMovieRequest): Fields and values to update for the matched movies. Only provided fields will be updated.
    Returns:
        SuccessResponse: A response object containing the number of matched and modified movies and a success message.
"""

@router.patch("/")
async def update_movies_batch(
    filter: MovieFilter,
    update: UpdateMovieRequest   
):
    movies_collection = get_collection("movies")

    filter_dict = filter.model_dump(exclude_unset=True, exclude_none=True)
    
    update_dict = update.model_dump(exclude_unset=True, exclude_none=True)

    #Verify the filter and the update dicts are not empty
    if not filter_dict or not update_dict:
        return create_error_response(
            message="Both filter and update objects are required",
            code="MISSING_REQUIRED_FIELDS",
            details=None
        )

    result = await movies_collection.update_many(filter_dict,{"$set": update_dict})
    
    return create_success_response({
        "matchedCount": result.matched_count,
        "modifiedCount": result.modified_count
        },
        f"Update operation completed. Matched {result.matched_count} movie(s), modified {result.modified_count} movie(s)."
)

#------------------------------------
# Place delete_movie endpoint here
#------------------------------------

#------------------------------------
# Place delete_movies_by_batch endpoint here
#------------------------------------
"""
    DELETE /api/movies/

    Delete multiple movies matching the given filter.

    Request Body:
        movie_filter (MovieFilter): Criteria to select which movies to delete. Only movies matching this filter will be removed.

        Returns:
        SuccessResponse: An object containing the number of deleted movies and a success message.
"""



@router.delete("/")
async def delete_movies_batch(movie_filter:MovieFilter):

    movies_collection = get_collection("movies")
    
    movie_filter_dict = movie_filter.model_dump(exclude_unset=True,exclude_none=True)

    if not movie_filter_dict:
        return create_error_response(
            message="Filter object is required and cannot be empty.",
            code="MISSING_FILTER",
            details=None
        )

    result = await movies_collection.delete_many(movie_filter_dict)

    return create_success_response(
        {"deletedCount":result.deleted_count},
        f'Delete operation completed. Removed {result.deleted_count} movies.'
    )



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