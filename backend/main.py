from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import json
from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from kafka import KafkaProducer
import os
import asyncio
import logging
import motor.motor_asyncio
import uuid
import time

app = FastAPI()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
MONGO_INITDB_ROOT_USERNAME = os.getenv("MONGO_INITDB_ROOT_USERNAME", "myuser")
MONGO_INITDB_ROOT_PASSWORD = os.getenv("MONGO_INITDB_ROOT_PASSWORD", "secret")
MONGO_HOSTNAME            = os.getenv("MONGO_HOSTNAME", "mongo")  # or just "mongo"
MONGO_CONNECTION_STRING   = (
    f"mongodb://{MONGO_INITDB_ROOT_USERNAME}:{MONGO_INITDB_ROOT_PASSWORD}"
    f"@{MONGO_HOSTNAME}:27017/?authSource=admin"
)

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
async def startup_db_client():
    app.mongodb_client = motor.motor_asyncio.AsyncIOMotorClient(MONGO_CONNECTION_STRING)
    app.database = app.mongodb_client["langton_ant"] 
    app.collection = app.database["simulations"]

@app.on_event("shutdown")
async def shutdown_db_client():
    app.mongodb_client.close()

@app.on_event("startup")
async def initialize_variables():
    app.state.save_simulation = False
    app.state.simulation_name = None
    app.state.simulation_id = None

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
    while True:
        await consumer.start()
        try:
            cells = []
            time = -1
            async for msg in consumer:
                data_str = msg.value.decode('utf-8')
                print("Got simulation update from Kafka:", data_str)
                try:
                    data = json.loads(data_str)
                    saveSimulation = data.get("saveSimulation", False)
                    if not ( time == -1 or data["time"] == time ):
                        aggregated_data = {"time": time, "cells": cells}
                        aggregated_json = json.dumps(aggregated_data)
                        print("Aggregated simulation updates:", aggregated_json)
                        for ws in clients:
                            await ws.send_text(aggregated_json)
                        if app.state.save_simulation:
                            aggregated_json = json.loads(aggregated_json)
                            aggregated_json["simulation_id"] = app.state.simulation_id
                            aggregated_json["simulation_name"] = app.state.simulation_name
                            app.database.simulations.insert_one(aggregated_json)
                        cells.clear()
                    time = data["time"]
                    del data["time"]
                    cells.append(data)
                except json.JSONDecodeError as e:
                    print(f"Error decoding JSON: {e}")
                    continue #skip bad json.
        finally:
            await consumer.stop()
            print(f"Stopped kafka consumer, sleeping before trying again")
            time.sleep(3)

@app.get("/")
async def get():
    return {"message": "Hello World"}

@app.get("/simulations")
async def get_simulations():
    simulations = []
    async for simulation in app.collection.find():
        simulation["_id"] = str(simulation["_id"])
        simulations.append(simulation)
    return {"simulations": simulations}

@app.get("/simulations/names")
async def get_simulation_names():
    unique_names = await app.collection.distinct("simulation_name")
    return unique_names

@app.get("/simulations/{simulation_name}")
async def get_simulation_steps(simulation_name: str):
    simulations = []
    async for simulation in app.collection.find({"simulation_name": simulation_name}):
        simulation["_id"] = str(simulation["_id"])
        simulations.append(simulation)
    return {"simulations": simulations}
        

# @app.get("/initialize_grid")
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
                saveSimulation = command.get("saveSimulation", False)
                simulationName = command.get("simulationName", "default")
                app.state.save_simulation = saveSimulation
                app.state.simulation_name = simulationName
                app.state.simulation_id = str(uuid.uuid4())

                print(f"Initializing grid with: rows={grid_rows}, cols={grid_cols}, ants={ants}")
                # logger.info(f"Initializing grid with: rows={grid_rows}, cols={grid_cols}, ants={ants}")
                command_data = {
                    "action": "initialize_grid",
                    "gridRows": grid_rows,
                    "gridCols": grid_cols,
                    "ants": ants,
                    "saveSimulation": saveSimulation,
                    "simulationName": simulationName
                } 
                await initialize_grid(websocket, command_data)

            if command['action'] == 'update_grid':
                await websocket.send_text("Update grid")

            if command["action"] == "get_simulations":
                print("Getting simulations")
                data = await get_simulation_names() 
                response = {
                    "action": "get_simulations_response",
                    "simulation_names": data
                }
                await websocket.send_text(json.dumps(response))

            if command["action"] == "get_simulation_data":
                simulation_name = command.get("simulation_name")
                print(f"Getting simulation data for name: {simulation_name}")
                data = await get_simulation_steps(simulation_name)
                if "error" in data:
                    response = {
                        "action": "get_simulation_data_response",
                        "error": data["error"]
                    }
                else:
                    for simulation in data["simulations"]:
                        simulation["_id"] = str(simulation["_id"])
                        await websocket.send_text(json.dumps(simulation))
                        await asyncio.sleep(0.2)  

    except WebSocketDisconnect:
        clients.remove(websocket)
        print("Client disconnected.")
