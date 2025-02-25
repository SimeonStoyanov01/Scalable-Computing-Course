function StopButton({simulationRunning, setSimulationStatus}) {
  return (
    <button disabled={!simulationRunning} onClick={(e) => setSimulationStatus(false)} >
      Stop
    </button>
  );
}

export default StopButton;