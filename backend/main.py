from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import json
from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from kafka import KafkaProducer
import os
import asyncio
import logging

app = FastAPI()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

try:
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )
    logger.info("Kafka Producer successfully initialized")
except Exception as e:
    logger.error(f"Failed to initialize Kafka Producer: {e}")
    producer = None

clients = []

@app.on_event("startup")
async def startup_event():
    # Start an async task that runs the AIOKafkaConsumer
    asyncio.create_task(kafka_listener())

async def kafka_listener():
    consumer = AIOKafkaConsumer(
        "langton_ant_updates",
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        auto_offset_reset="earliest"
    )
    await consumer.start()
    try:
        cells = []
        time = -1
        async for msg in consumer:
            data_str = msg.value.decode('utf-8')
            print("Got simulation update from Kafka:", data_str)
            try:
                data = json.loads(data_str)
                if not ( time == -1 or data["time"] == time ):
                    aggregated_data = {"time": time, "cells": cells}
                    aggregated_json = json.dumps(aggregated_data)
                    print("Aggregated simulation updates:", aggregated_json)
                    for ws in clients:
                        await ws.send_text(aggregated_json)
                    cells.clear()
                time = data["time"]
                del data["time"]
                cells.append(data)
            except json.JSONDecodeError as e:
                print(f"Error decoding JSON: {e}")
                continue #skip bad json.
    finally:
        await consumer.stop()

@app.get("/")
async def get():
    return {"message": "Hello World"}

@app.get("/initialize_grid")
async def initialize_grid(websocket: WebSocket, data: dict):
    print("Initializing grid")
    # print(data)
    producer.send("jobs", value=data)
    producer.flush()
    return {"message": "Grid initialized"}


@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    clients.append(websocket)
    print("Client connected.")
    try:
        while True:
            data = await websocket.receive_text()
            command = json.loads(data)
            print(f'Command received: {command}')
            # logger.info(f"Received command on WebSocket: {command}")

            if command['action'] == 'initialize_grid':
                grid_rows = command.get("gridRows")
                grid_cols = command.get("gridCols")
                ants = command.get("ants")
                print(f"Initializing grid with: rows={grid_rows}, cols={grid_cols}, ants={ants}")
                # logger.info(f"Initializing grid with: rows={grid_rows}, cols={grid_cols}, ants={ants}")
                command_data = {
                    "action": "initialize_grid",
                    "gridRows": grid_rows,
                    "gridCols": grid_cols,
                    "ants": ants
                } 
                await initialize_grid(websocket, command_data)

            if command['action'] == 'update_grid':
                await websocket.send_text("Update grid")

    except WebSocketDisconnect:
        clients.remove(websocket)
        print("Client disconnected.")
