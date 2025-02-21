from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import json

app = FastAPI()

clients = []

@app.get("/")
async def get():
    return {"message": "Hello World"}

@app.get("/initialize_grid")
async def initialize_grid(websocket: WebSocket):
    await websocket.send_text("Initialize grid")
    return {"message": "Grid initialized"}


@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    clients.append(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            command = json.loads(data)
            print(f'Command received: {command}')

            if command['type'] == 'initialize_grid':
                grid_rows = command.get("gridRows")
                grid_cols = command.get("gridCols")
                ants = command.get("ants")
                print(f"Initializing grid with: rows={grid_rows}, cols={grid_cols}, ants={ants}")
                await initialize_grid(websocket)
            if command['type'] == 'update_grid':
                await websocket.send_text("Update grid")

    except WebSocketDisconnect:
        clients.remove(websocket)
        print("Client disconnected.")