from pymongo import AsyncMongoClient
from dotenv import load_dotenv
import os
import voyageai

load_dotenv()

client = AsyncMongoClient(os.getenv("MONGO_URI"))
db = client[os.getenv("MONGO_DB")]
voyageai.api_key = os.getenv("VOYAGE_API_KEY")

def get_collection(name:str):
    return db[name]