from dotenv import load_dotenv
import os

load_dotenv()

GROQ_API_KEY      = os.getenv("GROQ_API_KEY")
DATABASE_URL      = os.getenv("DATABASE_URL")
STORE_SERVICE_URL = os.getenv("STORE_SERVICE_URL", "http://localhost:8081")
