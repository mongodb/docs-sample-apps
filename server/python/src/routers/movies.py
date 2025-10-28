from fastapi import APIRouter, Query
from src.database.mongo_client import get_collection
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
    try:
        object_id = ObjectId(id)
    except InvalidId:
        return create_error_response(
            message="Invalid movie ID format",
            code="INTERNAL_SERVER_ERROR",
            details=f"The provided ID '{id}' is not a valid ObjectId"
        )

    movies_collection = get_collection("movies")
    try:
        movie = await movies_collection.find_one({"_id": object_id})
    except Exception as e:
        return create_error_response(
            message="Database error occurred",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )

    if movie is None:
        return create_error_response(
            message="Movie not found",
            code="INTERNAL_SERVER_ERROR",
            details=f"No movie found with ID: {id}"
        )

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
    try:
        cursor = movies_collection.find(filter_dict).sort(sort).skip(skip).limit(limit)    
        movies = []
        async for movie in cursor:
            if movie is None:
                continue  # Skip null movies instead of raising exception
            movie["_id"] = str(movie["_id"]) # Convert ObjectId to string

            # Ensure that the year field contains int value.
            if "year" in movie and not isinstance(movie["year"], int):
                cleaned_year = re.sub(r"\D", "", str(movie["year"]))
                try:
                    movie["year"] = int(cleaned_year) if cleaned_year else None
                except ValueError:
                    movie["year"] = None
            movies.append(movie)
    except Exception as e:
        return create_error_response(
            message="Database error occurred",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )

    # Return the results wrapped in a SuccessResponse    
    return create_success_response(movies, f"Found {len(movies)} movies.")

#------------------------------------
# Place create_movie endpoint here
#------------------------------------

"""
    POST /api/movies/
    Create a new movie.
    Request Body:
        movie (CreateMovieRequest): A movie object containing the movie data.
            See CreateMovieRequest model for available fields.
    Returns:
        SuccessResponse[Movie]: A response object containing the created movie data.
"""

@router.post("/", response_model=SuccessResponse[Movie], status_code=201)
async def create_movie(movie: CreateMovieRequest):
    # Pydantic automatically validates the structure
    movie_data = movie.model_dump(by_alias=True, exclude_none=True)
    
    movies_collection = get_collection("movies")
    try:
        result = await movies_collection.insert_one(movie_data)
    except Exception as e:
        return create_error_response(
            message="Database error occurred",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )
    
    # Verify that the document was created before querying it
    if not result.acknowledged:
        return create_error_response(
            message="Failed to create movie",
            code="INTERNAL_SERVER_ERROR",
            details="The database did not acknowledge the insert operation"
        )
    
    try:
        # Retrieve the created document to return complete data
        created_movie = await movies_collection.find_one({"_id": result.inserted_id})
    except Exception as e:
        return create_error_response(
            message="Database error occurred",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )
    
    if created_movie is None:
        return create_error_response(
            message="Movie creation verification failed",
            code="INTERNAL_SERVER_ERROR",
            details="Movie was created but could not be retrieved for verification"
        )

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
    
    try:
        result = await movies_collection.insert_many(movies_dicts)
    except Exception as e:
        return create_error_response(
            message="Database error occurred during batch creation",
            code="INTERNAL_SERVER_ERROR",
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
    try:
        object_id = ObjectId(id)
    except InvalidId:
        return create_error_response(
            message="Invalid movie ID format",
            code="INTERNAL_SERVER_ERROR",
            details=f"The provided ID '{id}' is not a valid ObjectId"
        )

    movies_collection = get_collection("movies")
    try:
        # Use deleteOne() to remove a single document
        result = await movies_collection.delete_one({"_id": object_id})
    except Exception as e:
        return create_error_response(
            message="Database error occurred",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )

    if result.deleted_count == 0:
        return create_error_response(
            message="Movie not found",
            code="INTERNAL_SERVER_ERROR", 
            details=f"No movie found with ID: {id}"
        )
    
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
    try:
        object_id = ObjectId(id)
    except InvalidId:
        return create_error_response(
            message="Invalid movie ID format",
            code="INTERNAL_SERVER_ERROR",
            details=f"The provided ID '{id}' is not a valid ObjectId"
        )

    movies_collection = get_collection("movies")
    # Use find_one_and_delete() to find and delete in a single atomic operation
    # This is useful when you need to return the deleted document
    # or ensure the document exists before deletion
    try:
        deleted_movie = await movies_collection.find_one_and_delete({"_id": object_id})
    except Exception as e:
        return create_error_response(
            message="Database error occurred",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )

    if deleted_movie is None:
        return create_error_response(
            message="Movie not found",
            code="INTERNAL_SERVER_ERROR", 
            details=f"No movie found with ID: {id}"
        )
    deleted_movie["_id"] = str(deleted_movie["_id"]) # Convert ObjectId to string
    
    return create_success_response(deleted_movie, "Movie found and deleted successfully")

async def execute_aggregation(pipeline: list) -> list:
    """Helper function to execute aggregation pipeline and return results"""
    print(f"Executing pipeline: {pipeline}")  # Debug logging
    
    movies_collection = get_collection("movies")
    # For the async Pymongo driver, we need to await the aggregate call
    cursor = await movies_collection.aggregate(pipeline)
    results = await cursor.to_list(length=None)  # Convert cursor to list to collect all data at once rather than processing data per document
    
    print(f"Aggregation returned {len(results)} results")  # Debug logging
    if len(results) <= 3:  # Log first few results for debugging
        for i, doc in enumerate(results):
            print(f"Result {i+1}: {doc}")
    
    return results

"""
    GET /api/movies/reportingByComments
    Aggregate movies with their most recent comments using MongoDB $lookup aggregation.
    Joins movies with comments collection to show recent comment activity.
    Query Parameters:
        limit (int, optional): Number of results to return (default: 10, max: 50).
        movie_id (str, optional): Filter by specific movie ObjectId.
    Returns:
        SuccessResponse[List[dict]]: A response object containing movies with their most recent comments.
"""

@router.get("/api/movies/reportingByComments", response_model=SuccessResponse[List[dict]])
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
            return create_error_response(
                message="Invalid movie ID format",
                code="INTERNAL_SERVER_ERROR",
                details="The provided movie_id is not a valid ObjectId"
            )
    
    # Add a multi-stage aggregation that:
    # 1. Filters movies by valid year range
    # 2. Joins with comments collection (like SQL JOIN)
    # 3. Filters to only movies that have comments
    # 4. Sorts comments by date and extracts most recent ones
    # 5. Sorts movies by their most recent comment date
    # 6. Shapes the final output with transformed comment structure
    
    pipeline = [
        # STAGE 1: $match - Initial Filter
        # Filter movies to only those with valid year data
        # Tip: Use $match early to reduce the initial dataset for better performance
        {
            "$match": {
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}
            }
        }
    ]
    
    # Add remaining pipeline stages
    pipeline.extend([
        # STAGE 2: $lookup - Join with the 'comments' Collection
        # This gives each movie document a 'comments' array containing all its comments
        {
            "$lookup": {
                "from": "comments",
                "localField": "_id",
                "foreignField": "movie_id",
                "as": "comments"
            }
        },
        # STAGE 3: $match - Filter Movies with at Least One Comment
        # This helps reduces dataset to only movies with user engagement
        {
            "$match": {
                "comments": {"$ne": []}
            }
        },
        # STAGE 4: $addFields - Add New Computed Fields
        {
            "$addFields": {
                # Add computed field 'recentComments' that extracts only the N most recent comments (up to 'limit')
                "recentComments": {
                    "$slice": [
                        {
                            "$sortArray": {
                                "input": "$comments",
                                "sortBy": {"date": -1}  # -1 = descending (newest first)
                            }
                        },
                        limit  # Number of comments to keep
                    ]
                },
                # Add computed field 'mostRecentCommentDate' that gets the date of the most recent comment (to use in the next $sort stage)
                "mostRecentCommentDate": {
                    "$max": "$comments.date"
                }
            }
        },
        # STAGE 5: $sort - Sort Movies by Most Recent Comment Date
        {
            "$sort": {"mostRecentCommentDate": -1}
        },
        # STAGE 6: $limit - Restrict Result Set Size
        # - If querying single movie: return up to 50 results
        # - If querying all movies: return up to 20 results
        # Tip: This prevents overwhelming the client with too much data
        {
            "$limit": 50 if movie_id else 20
        },
        # STAGE 7: $project - Shape Final Response Output
        {
            "$project": {
                # Include basic movie fields
                "title": 1,
                "year": 1,
                "genres": 1,
                "_id": 1,
                # Extract nested field: imdb.rating -> imdbRating
                "imdbRating": "$imdb.rating",
                # Use $map to reshape computed 'recentComments' field with cleaner field names
                "recentComments": {
                    "$map": {
                        "input": "$recentComments",
                        "as": "comment",
                        "in": {
                            "userName": "$$comment.name",      # Rename: name -> userName
                            "userEmail": "$$comment.email",    # Rename: email -> userEmail
                            "text": "$$comment.text",          # Keep: text
                            "date": "$$comment.date"           # Keep: date
                        }
                    }
                },
                # Calculate the total number of comments into 'totalComments' (not just 'recentComments')
                # Used in display (e.g., "Showing 5 of 127 comments")
                "totalComments": {"$size": "$comments"}
            }
        }
    ])
    # Execute the aggregation
    try:
        results = await execute_aggregation(pipeline)
    except Exception as e:
        return create_error_response(
            message="Database error occurred during aggregation",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )

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
    GET /api/movies/reportingByYear
    Aggregate movies by year with average rating and movie count.
    Reports yearly statistics including average rating and total movies per year.
    Returns:
        SuccessResponse[List[dict]]: A response object containing yearly movie statistics.
"""

@router.get("/api/movies/reportingByYear", response_model=SuccessResponse[List[dict]])
async def aggregate_movies_by_year():
    # Define aggregation pipeline to group movies by year with statistics
    # This pipeline demonstrates grouping, statistical calculations, and data cleaning

    # Add a multi-stage aggregation that:
    # 1. Filters movies by valid year range (data quality filter)
    # 2. Groups movies by release year and calculates statistics per year
    # 3. Shapes the final output with clean field names and rounded averages
    # 4. Sorts results by year (newest first) for chronological presentation
    
    pipeline = [
        # STAGE 1: $match - Data Quality Filter
        # Clean data: ensure year is an integer and within reasonable range
        # Tip: Filter early to reduce dataset size and improve performance
        {
            "$match": {
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}
            }
        },
        
        # STAGE 2: $group - Aggregate Movies by Year
        # Group all movies by their release year and calculate various statistics
        {
            "$group": {
                "_id": "$year",  # Group by year field
                "movieCount": {"$sum": 1},  # Count total movies per year
                
                # Calculate average rating (only for valid numeric ratings)
                "averageRating": {
                    "$avg": {
                        "$cond": [
                            {"$and": [
                                {"$ne": ["$imdb.rating", None]},           # Not null
                                {"$ne": ["$imdb.rating", ""]},             # Not empty string
                                {"$eq": [{"$type": "$imdb.rating"}, "double"]}  # Is numeric
                            ]},
                            "$imdb.rating",  # Include valid IMDB ratings
                            "$$REMOVE"       # Exclude invalid IMDB ratings
                        ]
                    }
                },
                
                # Find highest rating for the year (same validation as average rating)
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
                
                # Find lowest rating for the year (same validation as average and highest rating)
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
                
                # Sum total votes across all movies in the year
                "totalVotes": {"$sum": "$imdb.votes"}
            }
        },
        
        # STAGE 3: $project - Shape Final Output
        # Transform the grouped data into a clean, readable format
        {
            "$project": {
                "year": "$_id",  # Rename _id back to year because grouping was done by year but values were stored in _id
                "movieCount": 1,
                "averageRating": {"$round": ["$averageRating", 2]},  # Round to 2 decimal places
                "highestRating": 1,
                "lowestRating": 1,
                "totalVotes": 1,
                "_id": 0  # Exclude the _id field from output
            }
        },
        
        # STAGE 4: $sort - Sort by Year (Newest First)
        # Sort results in descending order to show most recent years first
        {"$sort": {"year": -1}}  # -1 = descending order
    ]

    # Execute the aggregation
    try:
        results = await execute_aggregation(pipeline)
    except Exception as e:
        return create_error_response(
            message="Database error occurred during aggregation",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )
    
    return create_success_response(
        results, 
        f"Aggregated statistics for {len(results)} years"
    )


"""
    GET /api/movies/reportingByDirectors
    Aggregate directors with the most movies and their statistics.
    Reports directors sorted by number of movies directed.
    Query Parameters:
        limit (int, optional): Number of results to return (default: 20, max: 100).
    Returns:
        SuccessResponse[List[dict]]: A response object containing director statistics.
"""

@router.get("/api/movies/reportingByDirectors", response_model=SuccessResponse[List[dict]])
async def aggregate_directors_most_movies(
    limit: int = Query(default=20, ge=1, le=100)
):
    # Define aggregation pipeline to find directors with the most movies
    # This pipeline demonstrates array unwinding, filtering, and ranking
    
    # Add a multi-stage aggregation that:
    # 1. Filters movies with valid directors and year data (data quality filter)
    # 2. Unwinds directors array to create separate documents per director
    # 3. Cleans director names by filtering out null/empty names
    # 4. Groups movies by individual director and calculates statistics per director
    # 5. Sorts directors based on movie count
    # 6. Limits results to top N directors
    # 7. Shapes the final output with clean field names and rounded averages

    pipeline = [
        # STAGE 1: $match - Initial Data Quality Filter
        # Filter movies that have director information and valid years
        {
            "$match": {
                "directors": {"$exists": True, "$ne": None, "$ne": []},  # Has directors array
                "year": {"$type": "number", "$gte": 1800, "$lte": 2030}  # Valid year range
            }
        },
        
        # STAGE 2: $unwind - Flatten Directors Array
        # Convert each movie's directors array into separate documents
        # Example: Movie with ["Director A", "Director B"] becomes 2 documents
        {
            "$unwind": "$directors"
        },
        
        # STAGE 3: $match - Clean Director Names
        # Filter out any null or empty director names after unwinding
        {
            "$match": {
                "directors": {"$ne": None, "$ne": ""}
            }
        },
        
        # STAGE 4: $group - Aggregate by Director
        # Group all movies by director name and calculate statistics
        {
            "$group": {
                "_id": "$directors",  # Group by individual director name
                "movieCount": {"$sum": 1},  # Count movies per director
                "averageRating": {"$avg": "$imdb.rating"}  # Average rating of director's movies
            }
        },
        
        # STAGE 5: $sort - Rank Directors by Movie Count
        # Sort directors by number of movies (highest first)
        {"$sort": {"movieCount": -1}},  # -1 = descending (most movies first)
        
        # STAGE 6: $limit - Restrict Results
        # Limit to top N directors based on user input
        {"$limit": limit},
        
        # STAGE 7: $project - Shape Final Output
        # Transform the grouped data into a clean, readable format
        {
            "$project": {
                "director": "$_id",  # Rename _id to director
                "movieCount": 1,
                "averageRating": {"$round": ["$averageRating", 2]},  # Round to 2 decimal places
                "_id": 0  # Exclude the _id field from output
            }
        }
    ]

    # Execute the aggregation
    try:
        results = await execute_aggregation(pipeline)
    except Exception as e:
        return create_error_response(
            message="Database error occurred during aggregation",
            code="INTERNAL_SERVER_ERROR",
            details=str(e)
        )
    
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

# ---- Place Vector Search Here ----

