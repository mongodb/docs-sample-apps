from pymongo import AsyncMongoClient
from dotenv import load_dotenv
import os

load_dotenv()

client = AsyncMongoClient(os.getenv("MONGO_URI"))
db =client[os.getenv("MONGO_DB")]
