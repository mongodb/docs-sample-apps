# Welcome to the Python Backend

TOC
1. [Overview](#overview)
2. [Getting Setup](#getting-setup)
3. [Learning FastAPI](#learning-fastapi)
4. [Exploring the Codebase](#exploring-the-codebase)
5. [Feature Parity Status](#feature-parity-status)
6. [Utilities and Nice to Know](#nice-to-know)


## Overview
We're porting our Express backend to Python/FastAPI to achieve **functional parity** - same endpoints, same responses, same MongoDB operations. This allows one frontend to work with multiple backends.


**Important**: We're not rewriting - we're replicating behavior exactly. So this means we are figuring out the pythonic way of doing things. You will find that we can accomplish things with Fast in fewer lines of code than in Express.

## Getting Setup
0. Install Python if you don't already have it. (*Depending on your version of python, you might need to use python3 and pip3 vs python and pip*)
1. Clone the main repo.
    ```sh
    https://github.com/mongodb/docs-sample-apps.git
    ```

2. Fetch all branches 

    ```sh
    git fetch --all
    ```

3. Checkout my branch 
    ```sh
    git checkout python-backend-setup
    ```

4. Verify you are on the right branch 
    ```sh
    git branch
    ```
5. In the root directory. /python, make a virtual environment. 
    ```sh
    python -m venv .venv
    ```
6. Activate the virtual environment 
    ```sh
    source .venv/bin/activate/
    ```
7. Navigate back to /python, install the required packages. 
    ```sh 
    pip install -r requirements.txt
    ```
8. Create an .env file at the /python root.
It should have this format:
    ```python
    MONGO_URI="place your connection string here"
    MONGO_DB="sample_mflix"
    ```
9. Navigate back to the root and start the server.
    ```sh
    fastapi dev main.py or uvicorn main:app --reload
    ``` 
    (NOTE: THESE WONT WORK IF THE VIRTUAL ENVIRONMENT IS NOT ACTIVATED)
10. Click the link in the terminal or visit ```localhost:8000/docs```
11. Try visiting ```localhost:8000/api/movies```.

    If the setup ran correctly, you should see data  🎉 

*I recommend having your atlas instance up to explore your data and query the db directly.*

**Troubleshooting**: If commands fail, ensure your virtual environment is activated (you should see `(.venv)` in your terminal prompt). 


## Learning FastAPI 
Before diving into the repo, I suggest spending some time with the official [FastAPI tutorial](https://fastapi.tiangolo.com/tutorial/).

You only need to read up to the 'Request Body' section to get comfortable. *The query parameters and path parameters section can be read later.*

Key Takeaways:
- Decorators (@app.get, @app.post,etc.)
- Query, path, and body parameters
- Pydantic validation/ serialization
- Automatic JSON responses
- Built-in docs and testing at '/docs'


## Exploring the Codebase
Once you have completed the tutorial, I would suggest exploring the code and just noticing the differences between Express and Python. The apps are setup similarly but there are some small differences.

### Architecture
|Layer|Purpose|Express Equivalent| Differences|
|:---|:-------|:----------|:------|
|Routes <br> `routers/movies.py`| Defines all /movies endpoints (GET, POST, PUT, etc.)| /controllers/movieController.ts |The movies.ts file inside the routes file in the Express backend it actually wiring up the endpoints. Fast handles this for us in the main.py in one line. ```app.include_router(movies.router, prefix="/api/movies", tags=["movies"])```|
|Models <br> `models/models.py`| Pydantic schema for validating and serializing request/response data.| /types/index.ts|There are some differences in how the models are constructed. Take note on how nested classes are handled.|
|Utils <br> `utils/errorHandler.py`| Centralized utilities for responses, error handling and MongoDB exception mapping.|utils/errorHandler.ts |Express requires devs to write exception handling and validation on their own. Pydanic handles the validation and exception handling is a bit cleaner in Fast. You will notice the most differences here.|
|Database <br>`database/mongo_client.py`| Handles the connection to the db|/config/database.ts| *The current database file does not have feature parity with the Express version*|


## Feature Parity Status
|Feature|Status|Owner|Notes|
|:------|:-----|:----|:----|
|Global Exception Handling| DONE| - |Found in Utils |
|JSON Response Matching| DONE | - |Found in Utils|
|CRUD- ```insertOne()```| Not Started| Angela| - |
|CRUD- ```insertMany()```| Not Started| Taylor| - |
|CRUD- ```findOne()```| Not Started| Angela| - |
|CRUD- ```find()```| DONE| - | Found in movies.py as ```get_all_movies()``` This is a good function to look at to understand query parameters, requests and responses. Your functions will be simpler than this. But that is a good base to start with.| 
|CRUD- ```updateOne()```| Not Started| Angela| - |
|CRUD- ```updateMany()```| Not Started| Taylor| - |
|CRUD- ```deleteOne()```| Not Started| Angela| - |
|CRUD- ```deleteMany()```| Not Started| Taylor| -|
|CRUD- ```findOne()```| Not Started| Angela| - |
|CRUD- ```findOneAndDelete()```| Not Started| Angela| This one is a bit harder but I figure this could be a fun challenge.|


## Nice to Know
### Utilities (errorHandler.py)
This module provides a parity replacement for the Express errorHandler.ts file. It provides the same response shapes and error handling and removes the middleware.

- create_success_response(data,message) - creates a success response with the same shape as the Express version.
- create_error_response(message,code,details) - creates a error response with the same shape as the Express version.
- parse_mongo_exception(exc) - ensures PyMongo exceptions return JSON identical to the Express Version
- register_error_handlers - hooks our error system into the Fast app

Fast automatically handles async and validation errors, so there is no need for asyncHandler or validateRequiredFields from the TS version.

### Useful Links
- [Main Repo]( https://github.com/mongodb/docs-sample-apps/tree/main )
- [Sample App Scoping Doc](https://docs.google.com/document/d/12dROckw_Cp0ku2IIGku-ch7MvEuBPo0V4Gs-5wQgeHQ/edit?tab=t.0)
- [Sample App Project Description & Breakdown Doc](https://docs.google.com/document/d/1xv2dmcNrT-HYk5TBE-KtVDPmW0274rpBZ0_0QmC66ac/edit?tab=t.0#heading=h.ki9tatw08ilc)