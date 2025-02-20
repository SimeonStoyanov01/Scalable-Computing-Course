import React, { useState, useEffect } from 'react';
import ant from './ant.jpg'
import './App.css';
import ControlPanel from './components/controlPanel'
import Grid from './components/grid';

function App() {
  const [gridRows, setGridRows] = useState(10);
  const [gridCols, setGridCols] = useState(10);
  const [ants, setAntNumber] = useState(5);
  const [saveSimulation, setSaveSimulation] = useState(false);
  const [simulationRunning, setSimulationStatus] = useState(false);

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

  return (
    <div className="App">
      <header className="App-header">
        <h1> Langton Ant Simulation </h1>
        {/* <img src={ant} alt="logo" /> */}
      </header>

      <div className='main-content'>
        <div className="left-panel">
          <h1>This is the grid spot</h1>
          <Grid gridRows={gridRows}
            gridCols={gridCols}/>
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
            setSimulationStatus={setSimulationStatus} />
        </div>
      </div>

    </div>
  );
}

export default App;
