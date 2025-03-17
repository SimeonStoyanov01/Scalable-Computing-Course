FROM python:3.10-slim

WORKDIR /backend

RUN apt-get update && apt-get install -y netcat-openbsd

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
RUN chmod +x wait-for.sh
EXPOSE 8000

CMD ["./wait-for.sh", "kafka", "9092", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
