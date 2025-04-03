import { useState, useEffect } from "react"

function ControlPanel({gridRows, setGridRows, 
        gridCols, setGridCols, 
        ants, setAntNumber, 
        saveSimulation, setSaveSimulation, 
        simulationRunning, setSimulationStatus, 
        simulationName, setSimulationName, 
        selectedSimulation, setSelectedSimulation, 
        simulationIDs, setSimulationIDs,
        initialize_grid, get_simulations, onMessage}) {
    
    const handleStart = () => {
        setSimulationStatus(true);
        initialize_grid({gridRows, gridCols, ants, saveSimulation, simulationName});
    }

    const handleFocus = () => {
        console.log("Dropdown focused => retrieving simulations");
        get_simulations();
    };
    
    const handleSimulationSelect = (e) => {
        const simId = e.target.value;
        setSelectedSimulation(simId);
        console.log("Selected simulation ID:", simId);
    };

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
                    Simulation Name: 
                    <input 
                    type="text" 
                    value={simulationName} 
                    onChange={(e) => setSimulationName(e.target.value)} 
                    disabled={simulationRunning}
                    />
                </label>
            </div>
            <hr/>

            <div>
                <label>Select a saved simulation:</label>
                <select
                value={selectedSimulation}
                onChange={handleSimulationSelect}
                onFocus={handleFocus}
                >
                <option value="">-- pick an option --</option>
                {simulationIDs.map((id) => (
                    <option key={id} value={id}>
                    {id}
                    </option>
                ))}
                </select>
            </div>

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