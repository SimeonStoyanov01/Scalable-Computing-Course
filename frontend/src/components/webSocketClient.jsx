import React, { useState, useEffect, useRef } from "react";

const useWebSocketClient = (url) => {
    const [connected, setConnected] = useState(false);
    const socketRef = useRef(null);
    const callbackRef = useRef(null);

    const onMessage = (callback) => {
        callbackRef.current = callback;
    };

    const connectWebSocket = () => {
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
            setTimeout(() => {
                connectWebSocket();
            }, 3000);
        };
    };

    useEffect(() => {
        connectWebSocket();
        return () => {
            if (socketRef.current) {
                socketRef.current.close();
            }
        };
    }, [url]);

    const initialize_grid = ({ gridRows, gridCols, ants, saveSimulation }) => {
        if (socketRef.current && connected) {
            console.log(
                "Sending grid initialization command to the server: gridRows=",
                gridRows,
                "gridCols=",
                gridCols,
                "ants=",
                ants,
                "saveSimulation=",
                saveSimulation
            );
            socketRef.current.send(
                JSON.stringify({
                    action: "initialize_grid",
                    gridRows: gridRows,
                    gridCols: gridCols,
                    ants: ants,
                    saveSimulation: saveSimulation,
                })
            );
        }
    };

    const get_simulations = async () => {
        if (socketRef.current && connected) {
            console.log("Sending get_simulations command to the server");
            socketRef.current.send(
                JSON.stringify({ action: "get_simulations" })
            );
        }
    };

    const get_simulation_data = async (simulation_name) => {
        if (socketRef.current && connected) {
            console.log("Sending get_simulation_data command to the server");
            socketRef.current.send(
                JSON.stringify({
                    action: "get_simulation_data",
                    simulation_name: simulation_name,
                })
            );
        }
    };

    return {
        connected,
        initialize_grid,
        get_simulations,
        get_simulation_data,
        onMessage,
    };
};

export default useWebSocketClient;