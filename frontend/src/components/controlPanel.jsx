import { useState, useEffect } from "react"

function ControlPanel({gridRows, setGridRows, 
        gridCols, setGridCols, 
        ants, setAntNumber, 
        saveSimulation, setSaveSimulation, 
        simulationRunning, setSimulationStatus, 
        simulationName, setSimulationName, 
        selectedSimulation, setSelectedSimulation, 
        simulationIDs, setSimulationIDs,
        numSteps, setNumSteps,
        checkpointInterval, setCheckpointInterval,
        index, setIndex,
        initialize_grid, get_simulations, get_simulation_data, stop_simulation, onMessage}) {
    
    const handleStart = () => {
        setSimulationStatus(true);
        if (selectedSimulation) {
            console.log("Selected simulation ID:", selectedSimulation);
            get_simulation_data(selectedSimulation);
        } else {
            initialize_grid({gridRows, gridCols, ants, saveSimulation, simulationName, numSteps, checkpointInterval});
        }
    }

    const handleStop = () => {
        setSimulationStatus(false);
        stop_simulation();
    }

    const handleFocus = () => {
        console.log("Dropdown focused => retrieving simulations");
        get_simulations();
    };
    
    const handleSimulationSelect = (e) => {
        const simId = e.target.value;
        setSelectedSimulation(simId);
        setSimulationName(simId);
        console.log("Selected simulation name:", simId);
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
                <label>
                    Number of Steps:
                    <input
                    type="number"
                    min="10"
                    value={numSteps}
                    onChange={(e) => setNumSteps(Number(e.target.value))}
                    disabled={simulationRunning}
                    />
                </label>
            </div>
            <hr/>

            <div>
                <label>
                    Checkpoint Interval:
                    <input
                    type="number"
                    min="1"
                    value={checkpointInterval}
                    onChange={(e) => setCheckpointInterval(Number(e.target.value))}
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
                <button disabled={!simulationRunning} onClick={(e) => handleStop()}>
                    Stop
                </button>
            </div>
        </div>
    )
}

export default ControlPanel;