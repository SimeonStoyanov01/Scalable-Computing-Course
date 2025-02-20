function StartButton( {simulationRunning, setSimulationStatus} ) {

    return (
        <button disabled={simulationRunning} onClick={(e) => setSimulationStatus(true)}>
            Start Simulation
        </button>
    );
}

export default StartButton;