from fastapi import APIRouter, Query, Body, HTTPException
from src.database.mongo_client import db, get_collection
from src.models.models import CreateMovieRequest, Movie, SuccessResponse
from typing import List
from datetime import datetime
from src.utils.errorHandler import create_success_response, create_error_response
import re
from bson import ObjectId
from bson.errors import InvalidId

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
    GET /api/movies/{id}
    Retrieve a single movie by its ID.
    Path Parameters:
        id (str): The ObjectId of the movie to retrieve.
    Returns:
        SuccessResponse[Movie]: A response object containing the movie data.
"""

@router.get("/{id}", response_model=SuccessResponse[Movie])
async def get_movie_by_id(id: str):
    # Validate ObjectId format
    object_id = ObjectId(id)

    # Use findOne() to get a single document by _id
    movie = await db.movies.find_one({"_id": object_id})

    movie["_id"] = str(movie["_id"]) # Convert ObjectId to string
    
    return create_success_response(movie, "Movie retrieved successfully")

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
    cursor = movies_collection.find(filter_dict).sort(sort).skip(skip).limit(limit)    
    movies = []
    async for movie in cursor:
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

"""
    POST /api/movies/
    Create a new movie.
    Request Body:
        title (str): The title of the movie.
        genre (str): The genre of the movie.
        year (int): The year the movie was released.
        min_rating (float): The minimum IMDB rating.
        max_rating (float): The maximum IMDB rating.
    Returns:
        SuccessResponse[Movie]: A response object containing the created movie data.
"""

@router.post("/", response_model=SuccessResponse[CreateMovieRequest], status_code=201)
async def create_movie(movie: CreateMovieRequest):
    # Pydantic will automatically validate the structure
    movie_data = movie.model_dump(by_alias=True, exclude_none=True)
    
    result = await db.movies.insert_one(movie_data)

    # Retrieve the created document to return complete data
    created_movie = await db.movies.find_one({"_id": result.inserted_id})

    created_movie["_id"] = str(created_movie["_id"]) # Convert ObjectId to string
    
    return create_success_response(created_movie, f"Movie '{movie_data['title']}' created successfully")

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
async def create_movies_batch(movies: List[CreateMovieRequest]):
    movies_collection = get_collection("movies")
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

#------------------------------------
# Place update_movies_by_batch endpoint here
#------------------------------------

#------------------------------------
# Place delete_movie endpoint here
#------------------------------------


"""
    DELETE /api/movies/{id}
    Delete a single movie by its ID.
    Path Parameters:
        id (str): The ObjectId of the movie to delete.
    Returns:
        SuccessResponse[dict]: A response object containing deletion details.
"""

@router.delete("/{id}", response_model=SuccessResponse[dict])
async def delete_movie_by_id(id: str):
    object_id = ObjectId(id)

    # Use deleteOne() to remove a single document
    result = await db.movies.delete_one({"_id": object_id})

    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Movie not found")
    
    return create_success_response(
        {"deletedCount": result.deleted_count}, 
        "Movie deleted successfully"
    )

#------------------------------------
# Place delete_movies_by_batch endpoint here
#------------------------------------

#------------------------------------
# Place find_and_delete_movie endpoint here
#------------------------------------

"""
    DELETE /api/movies/{id}/find-and-delete
    Finds and deletes a movie in a single atomic operation.
    Demonstrates the findOneAndDelete() operation.
    Path Parameters:
        id (str): The ObjectId of the movie to find and delete.
    Returns:
        SuccessResponse[Movie]: A response object containing the deleted movie data.
"""

@router.delete("/{id}/find-and-delete", response_model=SuccessResponse[Movie])
async def find_and_delete_movie(id: str):
    object_id = ObjectId(id)

    # Use find_one_and_delete() to find and delete in a single atomic operation
    # This is useful when you need to return the deleted document
    # or ensure the document exists before deletion
    deleted_movie = await db.movies.find_one_and_delete({"_id": object_id})

    if deleted_movie is None:
        raise HTTPException(status_code=404, detail="Movie not found")

    deleted_movie["_id"] = str(deleted_movie["_id"]) # Convert ObjectId to string
    
    return create_success_response(deleted_movie, "Movie found and deleted successfully")

async def execute_aggregation(pipeline: list) -> list:
    """Helper function to execute aggregation pipeline and return results"""
    print(f"Executing pipeline: {pipeline}")  # Debug logging
    print(f"Database name: {db.name if hasattr(db, 'name') else 'unknown'}")
    print(f"Collection name: movies")
    
    # For motor (async MongoDB driver), we need to await the aggregate call
    cursor = await db.movies.aggregate(pipeline)
    results = await cursor.to_list(length=None)  # Convert cursor to list
    
    print(f"Aggregation returned {len(results)} results")  # Debug logging
    if len(results) <= 3:  # Log first few results for debugging
        for i, doc in enumerate(results):
            print(f"Result {i+1}: {doc}")
    
    return results


"""
    GET /api/movies/aggregate/by-genre
    Aggregate movies by genre with statistics using MongoDB aggregation pipeline.
    Demonstrates grouping values from multiple documents and performing operations on grouped data.
    Returns:
        SuccessResponse[List[dict]]: A response object containing aggregated genre statistics.
"""

@router.get("/aggregate/by-genre", response_model=SuccessResponse[List[dict]])
async def aggregate_movies_by_genre():
    # Define an aggregation pipeline with match, unwind, group, and sort stages
    pipeline = [
        # Clean data: ensure year is an integer
        {
            "$match": {
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}
            }
        },
        {"$unwind": "$genres"},
        {
            "$group": {
                "_id": "$genres",
                "count": {"$sum": 1},
                "avgRating": {"$avg": "$imdb.rating"},
                "minYear": {"$min": "$year"},
                "maxYear": {"$max": "$year"},
                "totalVotes": {"$sum": "$imdb.votes"}
            }
        },
        {"$sort": {"count": -1}},
        {
            "$project": {
                "genre": "$_id",
                "movieCount": "$count",
                "averageRating": {"$round": ["$avgRating", 2]},
                "yearRange": {
                    "min": "$minYear",
                    "max": "$maxYear"
                },
                "totalVotes": "$totalVotes",
                "_id": 0
            }
        }
    ]

    # Execute the aggregation
    results = await execute_aggregation(pipeline)
    
    return create_success_response(
        results, 
        f"Aggregated statistics for {len(results)} genres"
    )


"""
    GET /api/movies/aggregate/recent-commented
    Aggregate movies with their most recent comments using MongoDB $lookup aggregation.
    Joins movies with comments collection to show recent comment activity.
    Query Parameters:
        limit (int, optional): Number of results to return (default: 10, max: 50).
        movie_id (str, optional): Filter by specific movie ObjectId.
    Returns:
        SuccessResponse[List[dict]]: A response object containing movies with their most recent comments.
"""

@router.get("/aggregate/recent-commented", response_model=SuccessResponse[List[dict]])
async def aggregate_movies_recent_commented(
    limit: int = Query(default=10, ge=1, le=50),
    movie_id: str = Query(default=None)
):
    # Define aggregation pipeline to join movies with their most recent comments
    pipeline = [
        {
            "$match": {
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}
            }
        }
    ]
    
    # Add movie_id filter if provided
    if movie_id:
        try:
            object_id = ObjectId(movie_id)
            pipeline[0]["$match"]["_id"] = object_id
        except Exception:
            raise HTTPException(status_code=400, detail="Invalid movie_id format")
    
    # Add lookup and additional pipeline stages
    pipeline.extend([
        {
            "$lookup": {
                "from": "comments",
                "localField": "_id",
                "foreignField": "movie_id",
                "as": "comments"
            }
        },
        {
            "$match": {
                "comments": {"$ne": []}
            }
        },
        {
            "$addFields": {
                "recentComments": {
                    "$slice": [
                        {
                            "$sortArray": {
                                "input": "$comments",
                                "sortBy": {"date": -1}
                            }
                        },
                        limit
                    ]
                },
                "mostRecentCommentDate": {
                    "$max": "$comments.date"
                }
            }
        },
        {
            "$sort": {"mostRecentCommentDate": -1}
        },
        {
            "$limit": 50 if movie_id else 20
        },
        {
            "$project": {
                "title": 1,
                "year": 1,
                "genres": 1,
                "imdbRating": "$imdb.rating",
                "recentComments": {
                    "$map": {
                        "input": "$recentComments",
                        "as": "comment",
                        "in": {
                            "userName": "$$comment.name",
                            "userEmail": "$$comment.email",
                            "text": "$$comment.text",
                            "date": "$$comment.date"
                        }
                    }
                },
                "totalComments": {"$size": "$comments"},
                "_id": 1
            }
        }
    ])

    # Execute the aggregation
    results = await execute_aggregation(pipeline)
    
    # Convert ObjectId to string for response
    for result in results:
        if "_id" in result:
            result["_id"] = str(result["_id"])
    
    # Calculate total comments from all movies
    total_comments = sum(result.get("totalComments", 0) for result in results)
    
    return create_success_response(
        results, 
        f"Found {total_comments} comments from movie{'s' if len(results) != 1 else ''}"
    )


"""
    GET /api/movies/aggregate/by-year
    Aggregate movies by year with average rating and movie count.
    Reports yearly statistics including average rating and total movies per year.
    Returns:
        SuccessResponse[List[dict]]: A response object containing yearly movie statistics.
"""

@router.get("/aggregate/by-year", response_model=SuccessResponse[List[dict]])
async def aggregate_movies_by_year():
    # Define aggregation pipeline to group movies by year
    pipeline = [
        # Clean data: ensure year is an integer and within reasonable range
        {
            "$match": {
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}
            }
        },
        # Group by year and calculate statistics
        {
            "$group": {
                "_id": "$year",
                "movieCount": {"$sum": 1},
                "averageRating": {
                    "$avg": {
                        "$cond": [
                            {"$and": [
                                {"$ne": ["$imdb.rating", None]},
                                {"$ne": ["$imdb.rating", ""]},
                                {"$eq": [{"$type": "$imdb.rating"}, "double"]}
                            ]},
                            "$imdb.rating",
                            "$$REMOVE"
                        ]
                    }
                },
                "highestRating": {
                    "$max": {
                        "$cond": [
                            {"$and": [
                                {"$ne": ["$imdb.rating", None]},
                                {"$ne": ["$imdb.rating", ""]},
                                {"$eq": [{"$type": "$imdb.rating"}, "double"]}
                            ]},
                            "$imdb.rating",
                            "$$REMOVE"
                        ]
                    }
                },
                "lowestRating": {
                    "$min": {
                        "$cond": [
                            {"$and": [
                                {"$ne": ["$imdb.rating", None]},
                                {"$ne": ["$imdb.rating", ""]},
                                {"$eq": [{"$type": "$imdb.rating"}, "double"]}
                            ]},
                            "$imdb.rating",
                            "$$REMOVE"
                        ]
                    }
                },
                "totalVotes": {"$sum": "$imdb.votes"}
            }
        },
        {
            "$project": {
                "year": "$_id",
                "movieCount": 1,
                "averageRating": {"$round": ["$averageRating", 2]},
                "highestRating": 1,
                "lowestRating": 1,
                "totalVotes": 1,
                "_id": 0
            }
        },
        {"$sort": {"year": -1}}
    ]

    # Execute the aggregation
    results = await execute_aggregation(pipeline)
    
    return create_success_response(
        results, 
        f"Aggregated statistics for {len(results)} years"
    )


"""
    GET /api/movies/aggregate/directors
    Aggregate directors with the most movies and their statistics.
    Reports directors sorted by number of movies directed.
    Query Parameters:
        limit (int, optional): Number of results to return (default: 20, max: 100).
        min_movies (int, optional): Minimum number of movies to include director (default: 1).
    Returns:
        SuccessResponse[List[dict]]: A response object containing director statistics.
"""

@router.get("/aggregate/directors", response_model=SuccessResponse[List[dict]])
async def aggregate_directors_most_movies(
    limit: int = Query(default=20, ge=1, le=100)
):
    # Define aggregation pipeline to find directors with most movies
    pipeline = [
        {
            "$match": {
                "directors": {"$exists": True, "$ne": None, "$ne": []},
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}
            }
        },
        {
            "$unwind": "$directors"
        },
        {
            "$match": {
                "directors": {"$ne": None, "$ne": ""}
            }
        },
        {
            "$group": {
                "_id": "$directors",
                "movieCount": {"$sum": 1},
                "averageRating": {"$avg": "$imdb.rating"}
            }
        },
        {"$sort": {"movieCount": -1}},
        {"$limit": limit},
        {
            "$project": {
                "director": "$_id",
                "movieCount": 1,
                "averageRating": {"$round": ["$averageRating", 2]},
                "_id": 0
            }
        }
    ]

    # Execute the aggregation
    results = await execute_aggregation(pipeline)
    
    return create_success_response(
        results, 
        f"Found {len(results)} directors with most movies"
    )

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