FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY config/ ./config/
COPY exceptions/ ./exceptions/
COPY models/ ./models/
COPY routes/ ./routes/
COPY schemas/ ./schemas/
COPY service/ ./service/
COPY main.py .

EXPOSE 8099

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8099"]
