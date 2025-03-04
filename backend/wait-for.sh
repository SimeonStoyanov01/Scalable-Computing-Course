#!/bin/sh
HOST="$1"
PORT="$2"
shift 2

echo "Waiting for Kafka ($HOST:$PORT)..."

until nc -z -v -w30 "$HOST" "$PORT"; do
  echo "Kafka is unavailable - sleeping"
  sleep 1
done

echo "Kafka is up - executing command"

exec "$@"
