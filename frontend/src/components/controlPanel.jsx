import { useState, useEffect } from "react"

function ControlPanel({gridRows, setGridRows, gridCols, setGridCols, ants, setAntNumber, saveSimulation, setSaveSimulation, simulationRunning, setSimulationStatus, initialize_grid}) {
    const handleStart = () => {
        setSimulationStatus(true);
        initialize_grid({gridRows, gridCols, ants});
    }

    return (
        <div>
            <h1> Control Panel</h1>

            <div>
                <label>
                    Grid Rows: 
                    <input 
                    type="number" 
                    min="1" 
                    value={gridRows} 
                    onChange={(e) => setGridRows(Number(e.target.value))} 
                    disabled={simulationRunning}
                    />
                </label>
            </div>
            <hr/>

            <div>
                <label>
                    Grid Columns: 
                    <input 
                    type="number" 
                    min="1" 
                    value={gridCols} 
                    onChange={(e) => setGridCols(Number(e.target.value))} 
                    disabled={simulationRunning}
                    />
                </label>
            </div>
            <hr/>

            <div>
                <label>
                    Ant Number: 
                    <input 
                    type="number" 
                    min="1" 
                    value={ants} 
                    onChange={(e) => setAntNumber(Number(e.target.value))} 
                    disabled={simulationRunning}
                    />
                </label>
            </div>
            <hr/>

            <div>
                <label>
                    Save Simulation: 
                    <input 
                    type="checkbox" 
                    defaultChecked={false}
                    checked={saveSimulation}
                    onChange={(e) => setSaveSimulation(e.target.checked)}
                    disabled={simulationRunning} />
                </label>
            </div>
            <hr/>

            <div>
                <button disabled={simulationRunning} onClick={(e) => handleStart()}>
                    Start Simulation
                </button>
            </div>

            <div>
                <button disabled={!simulationRunning} onClick={(e) => setSimulationStatus(false)}>
                    Stop
                </button>
            </div>
        </div>
    )
}

export default ControlPanel;