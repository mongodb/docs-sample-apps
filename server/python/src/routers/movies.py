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

    try:
        result = movies_collection.find(filter_dict).sort(sort).skip(skip).limit(limit)  
    except Exception as e:
        return create_error_response(
            message="An error occurred while fetching movies.",
            code="DATABASE_ERROR",
            details=str(e)
        )   

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

@router.post(
        "/batch",
        response_model=SuccessResponse[dict],
        status_code = 201,
        tags=["movies"],
        summary = "Create multiple movies"
        )
async def create_movies_batch(movies: List[CreateMovieRequest]) ->SuccessResponse[dict]:
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

    try:
        result = await movies_collection.insert_many(movies_dicts)
    except Exception as e:
        return create_error_response(
            message="An error occurred while inserting movies.",
            code="DATABASE_ERROR",
            details=str(e)
        )    
    
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
@router.patch(
        "/{movie_id}",
        response_model=SuccessResponse[Movie],
        status_code=200,
        tags=["movies"],
        summary="Update a single movie by its ID.")
async def update_movie(
    movie_data: UpdateMovieRequest,
    movie_id: str = Path(...)
) -> SuccessResponse[Movie]:

    movies_collection = get_collection("movies")
    
    # Validate the ObjectId
    try:
        movie_id = ObjectId(movie_id)
    except Exception :
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

    try:
        result = await movies_collection.update_one(
            {"_id": movie_id},
            {"$set":update_dict}
        )
    except Exception as e:
        return create_error_response(
            message="An error occurred while updating the movie.",
            code="DATABASE_ERROR",
            details=str(e)
        )    

    if result.matched_count == 0:
        return create_error_response(
            message="No movie with that _id was found.",
            code="MOVIE_NOT_FOUND",
            details=str(movie_id)
        )
    
    updatedMovie = await movies_collection.find_one({"_id": str(movie_id)})
    updatedMovie["_id"] = str(updatedMovie["_id"])

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

@router.patch("/",
        response_model=SuccessResponse[dict],
        status_code=200,
        tags=["movies"],
        summary="Batch update movies matching the given filter."
        )
async def update_movies_batch(
    filter: MovieFilter,
    update: UpdateMovieRequest   
) -> SuccessResponse[dict]:
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

    try:
        result = await movies_collection.update_many(filter_dict,{"$set": update_dict})
    except Exception as e:
        return create_error_response(
            message="An error occurred while updating movies.",
            code="DATABASE_ERROR",
            details=str(e)
        )
    
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

@router.delete(
        "/",
        response_model=SuccessResponse[dict],
        status_code=200,
        tags=["movies"],
        summary="Delete multiple movies matching the given filter."
)
async def delete_movies_batch(movie_filter:MovieFilter) -> SuccessResponse[dict]:

    movies_collection = get_collection("movies")
    movie_filter_dict = movie_filter.model_dump(exclude_unset=True,exclude_none=True)

    if not movie_filter_dict:
        return create_error_response(
            message="Filter object is required and cannot be empty.",
            code="MISSING_FILTER",
            details=None
        )

    try:
        result = await movies_collection.delete_many(movie_filter_dict)
    except Exception as e:
        return create_error_response(
            message="An error occurred while deleting movies.",
            code="DATABASE_ERROR",
            details=str(e)
        )

    return create_success_response(
        {"deletedCount":result.deleted_count},
        f'Delete operation completed. Removed {result.deleted_count} movies.'
    )



#------------------------------------
# Place find_and_delete_movie endpoint here
#------------------------------------





#------------------------------------
#  Atlas Search
#------------------------------------
"""
Atlas search based on searching the plot, fullplot, directors, writers, and cast fields.
This function was made with the assumption that the UI will have fields for plot,fullplot, 
directors, writers, and cast to search on. Or some sort of combined search field.
"""

@router.get(
    "/search/atlas",
    response_model=SuccessResponse[List[Movie]],
    tags=["movies"],
    summary="Search movies using Atlas Search."
)

async def search_movies_atlas(
    plot: str = Query(default=None),
    fullplot: str = Query(default=None),
    directors: str = Query(default=None),
    writers: str = Query(default=None),
    cast: str = Query(default=None),
    limit:int = Query(default=20, ge=1, le=100),
    skip:int = Query(default=0, ge=0),
    search_operator: str = Query(default="must")
) -> SuccessResponse[List[Movie]]:
    
    movies_collection = get_collection("movies")
    search_phrases = []

    # Build the search phrases based on provided parameters
    if plot:
        search_phrases.append({
            "text": {
                "query": plot,
                "path": "plot",
                "fuzzy":{"maxEdits":1, "prefixLength":2}
            }
        })
    if fullplot:
        search_phrases.append({
            "text": {
                "query": fullplot,
                "path": "fullplot",
                "fuzzy":{"maxEdits":1, "prefixLength":2}
            }
        })
    if directors:
        search_phrases.append({
            "text": {
                "query": directors,
                "path": "directors",
                "fuzzy":{"maxEdits":2, "prefixLength":2, "maxExpansions":50}

            }
        })
    if writers:
        search_phrases.append({
            "text": {
                "query": writers,
                "path": "writers",
                "fuzzy":{"maxEdits":1, "prefixLength":2}
            }
        })
    if cast:
        search_phrases.append({
            "text": {
                "query": cast,
                "path": "cast",
                "fuzzy":{"maxEdits":1, "prefixLength":2}
            }
        })

    if not search_phrases:
        return create_error_response(
            message="At least one search parameter must be provided.",
            code="NO_SEARCH_PARAMETERS",
            details=None
        )

    aggregation_pipeline = [
        {
            "$search": {
                "index": "movieSearchIndex",
                "compound": {
                    "must": search_phrases
                }
            }
        },
        {"$skip": skip},
        {"$limit": limit},

        {
            "$project": {
                "_id": 1,
                "title": 1,
                "year": 1,
                "plot": 1,
                "fullplot": 1,
                "released":1,
                "runtime": 1,
                "poster": 1,
                "genres": 1,
                "directors": 1,
                "writers": 1,
                "cast": 1,
                "countries": 1,
                "languages": 1,
                "rated": 1,
                "awards": 1,
                "imdb": 1,
            }
        }
    ]

    try:
        results = await execute_aggregation(aggregation_pipeline)
    except Exception as e:
        return create_error_response(
            message="An error occurred while performing the search.",
            code="DATABASE_ERROR",
            details=str(e)
        )    

    movies = []
    for movie in results:
        movie["_id"] = str(movie["_id"])
        movies.append(movie)

    return create_success_response(movies, f"Found {len(movies)} movies matching the search criteria.")
    


#------------------------------------
#Helper Functions
#------------------------------------

"""  
    Helper function to execute aggregation pipeline and return results.  

    Args:  
        pipeline: MongoDB aggregation pipeline stages  

    Returns:  
        List of documents from aggregation result  
"""  

async def execute_aggregation(pipeline: List[dict]) -> List[dict]:  

    print(f"Executing pipeline: {pipeline}")  
    
    movies_collection = get_collection("movies")  
    cursor = await movies_collection.aggregate(pipeline)  
    results = await cursor.to_list(length=None)  
    
    print(f"Aggregation returned {len(results)} results")  
    
    # Debug logging for small result sets  
    if len(results) <= 3:  
        for i, doc in enumerate(results, 1):  
            print(f"Result {i}: {doc}")  
    
    return results 