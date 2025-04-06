import React, { useState, useEffect, use } from 'react';
import ant from './ant.jpg'
import './App.css';
import ControlPanel from './components/controlPanel'
import Grid from './components/grid';
import useWebSocketClient from './components/webSocketClient';

function App() {
  
  const [grid, setGrid] = useState([]);
  const [gridRows, setGridRows] = useState(10);
  const [gridCols, setGridCols] = useState(10);
  const [ants, setAntNumber] = useState(5);
  const [saveSimulation, setSaveSimulation] = useState(false);
  const [simulationRunning, setSimulationStatus] = useState(false);
  const {connected, initialize_grid, get_simulations, get_simulation_data, stop_simulation, onMessage} = useWebSocketClient("ws://MY_APP_WEBSOCKET_URL/ws")
  const [updates, setUpdates] = useState(null);
  const [timestep, setTimestep] = useState(0);
  const [simulationName, setSimulationName] = useState("Simulation 1");
  const [selectedSimulation, setSelectedSimulation] = useState("");
  const [simulationIDs, setSimulationIDs] = useState([]);
  const [numSteps, setNumSteps] = useState(100);
  const [checkpointInterval, setCheckpointInterval] = useState(25);
  const [index, setIndex] = useState(1);
  const [newSimulation, setNewSimulation] = useState(true);

  useEffect(() => {
    console.log('Grid Rows: ', gridRows)
  }, [gridRows])

  useEffect(() => {
    console.log('Grid Columns: ', gridCols)
  }, [gridCols])

  useEffect(() => {
    console.log('Ant Number: ', ants)
  }, [ants])

  useEffect(() => {
    console.log('Save Simulation: ', saveSimulation)
  }, [saveSimulation])

  useEffect(() => {
    console.log('Simulation running: ', simulationRunning)
  }, [simulationRunning])

  useEffect(() => {
    console.log('Timestep: ', timestep)
  }, [timestep])

  useEffect(() => {
    console.log('Simulation Name: ', simulationName)
  }, [simulationName])

  useEffect(() => {
    console.log('Selected Simulation: ', selectedSimulation)
  }
  , [selectedSimulation])

  useEffect(() => {
    document.documentElement.style.setProperty("--rows", gridRows);
    document.documentElement.style.setProperty("--cols", gridCols);
  }, [gridRows, gridCols])
  
  useEffect(() => {
    onMessage((data) => {
      if (data.action === "get_simulations_response") {
        setSimulationIDs(data.simulation_names); 
    } else {
      setUpdates(data);
      setTimestep(data.time);
    }
    console.log("Received message from server:", data);
    });
  }, [onMessage]);

  return (
    <div className="App">
      <header className="App-header">
        <h1> Langton Ant Simulation    </h1>
        <div>
          <img src={ant} alt="Langton's Ant" className="header-image" />
        </div>
      </header>

      <div className='main-content'>
        <div className="left-panel">
          <Grid gridRows={gridRows}
            gridCols={gridCols} 
            updates={updates}
            grid={grid}
            setGrid={setGrid} />
        </div>

        <div className="right-panel">
          <ControlPanel gridRows={gridRows} 
            setGridRows={setGridRows} 
            gridCols={gridCols} 
            setGridCols={setGridCols}
            ants={ants}
            setAntNumber={setAntNumber}
            simulationRunning={simulationRunning}
            saveSimulation={saveSimulation}
            setSaveSimulation={setSaveSimulation}
            setSimulationStatus={setSimulationStatus} 
            simulationName={simulationName}
            setSimulationName={setSimulationName}
            initialize_grid={initialize_grid}
            get_simulations={get_simulations}
            simulationIDs={simulationIDs}
            setSimulationIDs={setSimulationIDs}
            selectedSimulation={selectedSimulation}
            setSelectedSimulation={setSelectedSimulation}
            get_simulation_data={get_simulation_data}
            numSteps={numSteps}
            setNumSteps={setNumSteps}
            checkpointInterval={checkpointInterval}
            setCheckpointInterval={setCheckpointInterval}
            stop_simulation={stop_simulation}
            index={index}
            setIndex={setIndex}
            newSimulation={newSimulation}
            setNewSimulation={setNewSimulation}
            grid={grid}
            setGrid={setGrid}
            onMessage={onMessage}/>

          <p>Simulation timestep: {timestep}</p>
        </div>
      </div>

      <footer>
        <p>WebSocket Status: {connected ? "Connected" : "Disconnected"}</p>
      </footer>
    </div>
  );
}

export default App;
