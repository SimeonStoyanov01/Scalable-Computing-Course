import React, { useState, useEffect } from 'react';
import ant from './ant.jpg'
import './App.css';
import ControlPanel from './components/controlPanel'
import Grid from './components/grid';
import useWebSocketClient from './components/webSocketClient';

function App() {
  const [gridRows, setGridRows] = useState(10);
  const [gridCols, setGridCols] = useState(10);
  const [ants, setAntNumber] = useState(5);
  const [saveSimulation, setSaveSimulation] = useState(false);
  const [simulationRunning, setSimulationStatus] = useState(false);
  const {connected, initialize_grid} = useWebSocketClient("ws://localhost:8000/ws")

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
    document.documentElement.style.setProperty("--rows", gridRows);
    document.documentElement.style.setProperty("--cols", gridCols);
  }, [gridRows, gridCols]);

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
            ants={ants} />
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
            initialize_grid={initialize_grid}/>
        </div>
      </div>

      <footer>
        <p>WebSocket Status: {connected ? "Connected" : "Disconnected"}</p>
      </footer>
    </div>
  );
}

export default App;
