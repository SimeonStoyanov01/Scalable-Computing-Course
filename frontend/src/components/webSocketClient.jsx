import React, { useState, useEffect, useRef } from "react";

const WebSocketClient = (url) => {
    const [ws, setWs] = useState(null);
    const [connected, setConnected] = useState(false);
    const socketRef = useRef(null);
    
    useEffect(() => {
        const ws = new WebSocket(url);
        socketRef.current = ws;

        ws.onopen = () => {
            console.log("Connected to the WebSocket server");
            setConnected(true);
        };
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log("Received from server:", data);
        };
        ws.onclose = () => {
            console.log("Disconnected from the WebSocket server");
            setConnected(false);
        };
        setWs(ws);
        return () => {
            if (socketRef.current) {
              socketRef.current.close();
            }
          };
    }, [url]);
    
    const initialize_grid = ({gridRows, gridCols, ants}) => {
        if (ws && connected) {
            ws.send(JSON.stringify({ action: "start" , 
                gridRows: gridRows, 
                gridCols: gridCols, 
                ants: ants
            }));
          }
    };
    
    return ({ws, connected, initialize_grid});
}

export default WebSocketClient;