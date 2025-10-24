from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional, TypeVar, Generic, Any


T = TypeVar("T")

class Awards(BaseModel):
    wins: Optional[int] = None
    nominations: Optional[int] = None
    text: Optional[str] = None

class Imdb(BaseModel):
    rating: Optional[float] = None
    votes: Optional[int] = None
    id: Optional[int] = None    

class Movie(BaseModel):
    id: Optional[str] = Field(alias="_id")
    title: str
    year: Optional[int] = None
    plot: Optional[str]  = None
    fullplot: Optional[str] = None
    released: Optional[datetime]  = None
    runtime: Optional[int]  = None
    poster: Optional[str]  = None
    genres: Optional[list[str]]  = None
    directors: Optional[list[str]]  = None
    writers: Optional[list[str]]  = None
    cast: Optional[list[str]]  = None
    countries: Optional[list[str]]  = None
    languages: Optional[list[str]]  = None
    rated: Optional[str]  = None
    awards: Optional[Awards] = None
    imdb: Optional[Imdb] = None

    model_config = {
        "populate_by_name" : True
    }


'''
So this an interesting conversion. Pydanic doesn't cleanly support constructing
models that have MongoDB query operators as field names. This becomes an issue when
we want to convert the validated model back to a dictionary to use as a MongoDB query filter.
For example, if a user leaves the 'q' parameter blank, we don't want to include the '$text' operator
in the filter at all but validation will send an empty value for it and that causes errors. So
I am handling the validation in the query router itself, but leaving this here as an example of how
it could be done, if I am wrong about Pydantic's capabilities.
'''

class TextFilter(BaseModel):
    search: str = Field(..., alias="$search")

class RegexFilter(BaseModel):
    regex: str = Field(..., alias="$regex")
    options: Optional[str] = Field(None, alias="$options")    

class RatingFilter(BaseModel):
    gte: Optional[float] = Field(None, alias="$gte")
    lte: Optional[float] = Field(None, alias="$lte")    

class Pagination(BaseModel):
    page: int
    limit: int
    total: int
    pages: int

class CreateMovieRequest(BaseModel):
    title: str
    year: Optional[int] = None
    plot: Optional[str]  = None
    fullplot: Optional[str] = None
    genres: Optional[list[str]]  = None
    directors: Optional[list[str]]  = None
    writers: Optional[list[str]]  = None
    cast: Optional[list[str]]  = None
    countries: Optional[list[str]]  = None
    languages: Optional[list[str]]  = None
    rated: Optional[str]  = None
    runtime: Optional[int]  = None
    poster: Optional[str]  = None    

class UpdateMovieRequest(BaseModel):
    title: Optional[str] = None
    year: Optional[int] = None
    plot: Optional[str]  = None
    fullplot: Optional[str] = None
    genres: Optional[list[str]]  = None
    directors: Optional[list[str]]  = None
    writers: Optional[list[str]]  = None
    cast: Optional[list[str]]  = None
    countries: Optional[list[str]]  = None
    languages: Optional[list[str]]  = None
    rated: Optional[str]  = None
    runtime: Optional[int]  = None
    poster: Optional[str]  = None  

class MovieFilter(BaseModel):
    title: Optional[str] = None
    year: Optional[int] = None
    plot: Optional[str]  = None
    fullplot: Optional[str] = None
    genres: Optional[list[str]]  = None
    directors: Optional[list[str]]  = None
    writers: Optional[list[str]]  = None
    cast: Optional[list[str]]  = None
    countries: Optional[list[str]]  = None
    languages: Optional[list[str]]  = None
    rated: Optional[str]  = None
    runtime: Optional[int]  = None
    poster: Optional[str]  = None   

class SuccessResponse(BaseModel, Generic[T]):
    success: bool = True
    message: Optional[str]
    data: T
    timestamp: str
    pagination: Optional[Pagination] = None


class ErrorDetails(BaseModel):
    message: str
    code: Optional[str]
    details: Optional[Any] = None

class ErrorResponse(BaseModel):
    success: bool = False
    message: str
    error: ErrorDetails
    timestamp: str
    


"""
I don't think we need this any more but we will see.

class MovieFilter(BaseModel):
    text: Optional[TextFilter] = Field(None, alias="$text")    
    genres: Optional[RegexFilter] = None
    year: Optional[int] = None
    imdb_rating: Optional[RatingFilter] = Field(None, alias="imdb.rating")

    model_config = {
        "populate_by_name" : True
    }

"""