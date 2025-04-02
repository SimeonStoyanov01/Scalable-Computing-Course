#!/bin/bash


num_ants=( 1 2 50 100 200 300 400 500 600 700 800 900 1000 2000 3000 4000 5000 6000 7000 8000 9000 10000 11000 12000 13000 14000 15000)
# checkpoint_intervals=( 1 2 5 10 25 50 100 200)
# num_ants=( 1 )

for num_ant in "${num_ants[@]}"; do
# for checkpoint_interval in "${checkpoint_intervals[@]}"; do
    echo "$(date '+%H:%M:%S') running with num_ant = $num_ant" >> simtimeslogs.txt
    until ./install-simulator-chart.sh 200 200 ${num_ant} 50 25 5 | tee >(stdbuf -oL grep 'Simulation time:' > results/simtimescheckpoint$num_ant.txt); do
        echo "$(date '+%H:%M:%S') trial failed trying again" >> simtimeslogs.txt
        sleep 5
    done
    sleep 10
    
    # ./install-simulator-chart.sh 100 100 1 100 ${checkpoint_interval} | tee /dev/stderr | grep 'Simulation time:' >> simtimes1.txt || true
    # ./install-simulator-chart.sh 100 100 1 100 ${checkpoint_interval} | tee /dev/stderr | grep 'Simulation time:' >> simtimes2.txt || true
    # ./install-simulator-chart.sh 100 100 1 100 ${checkpoint_interval} | tee /dev/stderr | grep 'Simulation time:' >> simtimes3.txt || true
    # ./install-simulator-chart.sh 200 200 ${num_ant} 5 25 | tee /dev/stderr | grep 'Simulation time:' >> simtimes0.txt
    # ./install-simulator-chart.sh 200 200 ${num_ant} 5 25 | tee /dev/stderr | grep 'Simulation time:' >> simtimes1.txt
    # ./install-simulator-chart.sh 200 200 ${num_ant} 5 25 | tee /dev/stderr | grep 'Simulation time:' >> simtimes2.txt
    # ./install-simulator-chart.sh 200 200 ${num_ant} 5 25 | tee /dev/stderr | grep 'Simulation time:' >> simtimes3.txt
    # ./install-simulator-chart.sh 200 200 ${num_ant} 5 25 | tee /dev/stderr | grep 'Simulation time:' >> simtimes4.txt
done