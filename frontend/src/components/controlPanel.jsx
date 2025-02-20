import { useState, useEffect } from "react"
import StartButton from "./startButton"
import StopButton from "./stopButton"

function ControlPanel({gridRows, setGridRows, gridCols, setGridCols, ants, setAntNumber, saveSimulation, setSaveSimulation, simulationRunning, setSimulationStatus}) {

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
                    name="saveCheckbox" 
                    defaultChecked={false}
                    checked={saveSimulation}
                    onChange={(e) => setSaveSimulation(e.target.checked)}
                    disabled={simulationRunning} />
                </label>
            </div>
            <hr/>

            <StartButton simulationRunning={simulationRunning}
                setSimulationStatus={setSimulationStatus} />
            <StopButton simulationRunning={simulationRunning}
                setSimulationStatus={setSimulationStatus} />
        </div>
    )
}

export default ControlPanel;