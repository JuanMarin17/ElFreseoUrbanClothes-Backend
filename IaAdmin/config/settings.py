from dotenv import load_dotenv
import os

load_dotenv()

GEMINI_API_KEY         = os.getenv("GEMINI_API_KEY")
GROQ_API_KEY           = os.getenv("GROQ_API_KEY")
DATABASE_URL           = os.getenv("DATABASE_URL")
STORE_SERVICE_URL      = os.getenv("STORE_SERVICE_URL",      "http://localhost:8081")
PRODUCT_SERVICE_URL    = os.getenv("PRODUCT_SERVICE_URL",    "http://localhost:8084")
REPORTS_SERVICE_URL    = os.getenv("REPORTS_SERVICE_URL",    "http://localhost:8100")
INVENTORY_SERVICE_URL  = os.getenv("INVENTORY_SERVICE_URL",  "http://localhost:8089")
SUPPORT_SERVICE_URL    = os.getenv("SUPPORT_SERVICE_URL",    "http://localhost:8088")
PROMOTIONS_SERVICE_URL = os.getenv("PROMOTIONS_SERVICE_URL", "http://localhost:8091")
