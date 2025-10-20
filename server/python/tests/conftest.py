import os
import pytest
from httpx import AsyncClient
from src.main import app
from unittest.mock import AsyncMock, MagicMock

os.environ["ENV"] = "test"
os.environ["MONGO_DB_NAME"] = "sample_mflix"
os.environ["MONGO_URI"] = "mock://no-connection"

#---------------------------------
# # 1. Setup Environment for Tests
#---------------------------------

@pytest.fixture(scope="session", autouse=True)
def set_test_env():
    print("\n [Setup] Running test environment setup...")
    yield
    print("\n [Teardown] Tests completed. Cleaning up...")

#---------------------------
# 2. Mock MongoDB Collection
#---------------------------

@pytest.fixture()
def mock_movies_collection():
    mock_collection = MagicMock()
    mock_collection.find = AsyncMock()


@pytest.fixture()
def client():
    with AsyncClient(app=app, base_url="http://test") as client:
        yield client 