from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import json
# from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
# from kafka import KafkaProducer
import os
import asyncio
import logging

app = FastAPI()

# logging.basicConfig(level=logging.INFO)
# logger = logging.getLogger(__name__)

# KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

# try:
#     producer = KafkaProducer(
#         bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
#         value_serializer=lambda v: json.dumps(v).encode('utf-8')
#     )
#     logger.info("Kafka Producer successfully initialized")
# except Exception as e:
#     logger.error(f"Failed to initialize Kafka Producer: {e}")
#     producer = None

clients = []

# @app.on_event("startup")
# async def startup_event():
#     # Start an async task that runs the AIOKafkaConsumer
#     asyncio.create_task(kafka_listener())

# async def kafka_listener():
#     consumer = AIOKafkaConsumer(
#         "langton_ant_updates",
#         bootstrap_servers="kafka:9092",
#         auto_offset_reset="earliest"
#     )
#     await consumer.start()
#     try:
#         async for msg in consumer:
#             data_str = msg.value.decode('utf-8')
#             print("Got simulation update from Kafka:", data_str)
#             # We are inside an async function, so we can do:
#             for ws in clients:
#                 await ws.send_text(data_str)
#     finally:
#         await consumer.stop()

@app.get("/")
async def get():
    return {"message": "Hello World"}

@app.get("/initialize_grid")
async def initialize_grid(websocket: WebSocket, data: dict):
    print("Initializing grid")
    print(data)
    # producer.send("initialize_grid", value=data)
    # producer.flush()

    data1 = {
	"time": 8,
  "cells": [
    {
      "colour": False,
      "ants": [ ],
      "rowInd": 0,
      "colInd": 1,
    },
    {
      "colour": False,
      "ants": [ ],
      "rowInd": 0,
      "colInd": 2,
    },
    {
      "colour": False,
      "ants": [
        {
          "direction": "North"
        }
      ],
      "rowInd": 1,
      "colInd": 0,
    },
    {
        "colour": True,
        "ants": [],
        "rowInd" : 1,
        "colInd" : 1,
    },
    {
        "colour": False,
        "ants": [],
        "rowInd" : 1,
        "colInd" : 2,
    },
    {
        "colour": True,
        "ants": [],
        "rowInd" : 2,
        "colInd" : 0,
    },
    {
      "colour": False,
      "ants": [
        {
          "direction": "West"
        },
        {
          "direction": "South"
        }
      ],
      "rowInd": 2,
      "colInd": 1,
    },
    {
      "colour": False,
      "ants": [],
      "rowInd": 2,
      "colInd": 2,
    },
    {
      "colour": False,
      "ants": [],
      "rowInd": 0, "colInd": 0,
    },
	]
}

    await websocket.send_text(json.dumps(data1))

    data2 = {
	"time": 9,
    "cells": [
    {
        "colour": False,
        "ants": [
					{
						"direction": "East"
					},
				],
        "rowInd" : 0,
        "colInd" : 0,
    },
    {
      "colour": True,
      "ants": [],
      "rowInd": 1,
      "colInd": 0,
    },
    {
		"colour": True,
		"ants": [
			{
				"direction": "South"
			},
		],
		"rowInd" : 2,
		"colInd" : 0,
    },
    {
      "colour": True,
      "ants": [],
      "rowInd": 2,
      "colInd": 1,
    }
  ]
}
    await asyncio.sleep(5)
    await websocket.send_text(json.dumps(data2))

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