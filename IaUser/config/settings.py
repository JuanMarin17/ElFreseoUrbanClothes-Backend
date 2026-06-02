from dotenv import load_dotenv
import os

load_dotenv()

GEMINI_API_KEY        = os.getenv("GEMINI_API_KEY")
GROQ_API_KEY          = os.getenv("GROQ_API_KEY")
DATABASE_URL          = os.getenv("DATABASE_URL")
PRODUCT_SERVICE_URL   = os.getenv("PRODUCT_SERVICE_URL",    "http://localhost:8084")
CART_SERVICE_URL      = os.getenv("CART_SERVICE_URL",       "http://localhost:8083")
SUPPORT_SERVICE_URL   = os.getenv("SUPPORT_SERVICE_URL",    "http://localhost:8088")
STORE_SERVICE_URL     = os.getenv("STORE_SERVICE_URL",      "http://localhost:8081")
PROMOTIONS_SERVICE_URL = os.getenv("PROMOTIONS_SERVICE_URL","http://localhost:8091")
REVIEWS_SERVICE_URL   = os.getenv("REVIEWS_SERVICE_URL",    "http://localhost:8095")
CMS_SERVICE_URL       = os.getenv("CMS_SERVICE_URL",        "http://localhost:8101")
PREFERENCES_SERVICE_URL = os.getenv("PREFERENCES_SERVICE_URL", "http://localhost:8094")
