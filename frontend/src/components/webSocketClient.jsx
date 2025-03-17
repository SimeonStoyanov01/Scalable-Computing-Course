import React, { useState, useEffect, useRef } from "react";

const useWebSocketClient = (url) => {
    // const [ws, setWs] = useState(null);
    const [connected, setConnected] = useState(false);
    const socketRef = useRef(null);

    const callbackRef = useRef(null);

    const onMessage = (callback) => {
        callbackRef.current = callback;
    };
    
    useEffect(() => {
        const ws = new WebSocket(url);
        socketRef.current = ws;

        ws.onopen = () => {
            console.log("Connected to the WebSocket server");
            setConnected(true);
        };
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            if (callbackRef.current) {
                callbackRef.current(data);
            }
        };
        ws.onclose = () => {
            console.log("Disconnected from the WebSocket server");
            setConnected(false);
        };
        // setWs(ws);
        return () => {
            if (socketRef.current) {
              socketRef.current.close();
            }
          };
    }, [url]);
    
    const initialize_grid = ({gridRows, gridCols, ants}) => {
        if (socketRef.current && connected) {
            console.log("Sending grid initialization command to the server: gridRows=", gridRows, "gridCols=", gridCols, "ants=", ants);
            socketRef.current.send(JSON.stringify({ action: "initialize_grid", 
                gridRows: gridRows, 
                gridCols: gridCols, 
                ants: ants
            }));
          }
    };
    
    return ({ connected, initialize_grid, onMessage });
}

export default useWebSocketClient;