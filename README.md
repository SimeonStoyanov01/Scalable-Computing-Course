# Scalable computing - Report

## Introduction
Langton's Ant is a simple automation, that allows for measuring algorithmic complexity, through the simple rules that an ant follows, while travesing a grid. Based on the rules defined and the number of steps completed, the ant forms intricate patterns overtime. While the concept is fascinating and it presents some peculiarity being related to the subject of ants, the simulation of multiple Langton Ants traversing both small and large grids could present a great computational challenge. The purpose of this project is to create a scalable framework, capable of simulating multiple ants concurrently, without facing performance bottlenecks with the increase of workload. 

In the context of scalable computing, the project is an attempt at creating a distributed system that is designed to bypass the single point of failure risk when deploying an application on a single node. By deploying the application in a cluster environment using Kubernetes, we aim to ensure that the framework would distribute workloads to resources equally, make it fault tollerant and easily scalable horizontally.

This report will outline our efforts while designing the application, our thought process when tackling every objective of the system design, the actual implementation details as well as thoughts about its limitations and future work.

## Project Overview

<!-- Explain the general outline of the project and how we tackled it -->

<!-- Explain core functionality; what can be done through our app -->

## Usage?

## Data pipelines

### Streamline data #Robin?

<!-- Include pipeline figure if you can -->

### Historical data #Carmen

<!-- Include pipeline figure if you can -->


## Infrastructure Setup

### Simulation - Spark #Robin

### Kafka

### Backend #Carmen

The backend is a core structural component for the application, responsible for accumulating, aggregating and serving data to the frontend. For its implementation, we have chosen the FastAPI framework, which is a Python, modern, fast, high-performance web framework for building APIs. FastAPI is an ideal choice for this application as it is an asynchronous framework that can handle a vast number of requests concurrently. The backend makes use of a websocket for streaming data in real-time to the frontend.

The backend is responsible for several tasks, including:
- Communicating with the simulation in order to signal the start/stop actions, as well as transmitting the simulation parameters.
- Fetching data from the Kafka topic `langton_ant_updates`, aggregating said results and  sending then to the frontend.
- Save historical data to the database if the user requests it.
- Query the database for historical data and serve it to the frontend.

### Frontend #Carmen

The frontend represents the first point of access for the user to interact with the application. It displays the state of the simulation in real-time, as well as offering a set of controls to manipulate the simulation. This component has been implemented using React, a JavaScript library for constructing responsive and dynamic user interfaces. This design choice comes naturally as React enables the utilisation of reusable components for fast rendering of real-time updates of the state of the simulation, as well as providing native intergration for websocket communication with the backend.

We have designed the user interface with a focus on simplicity and intuitive interaction. The user may modify the parameters of the simulation, consisting of the grid size (rows and columns) and the number of ants. Furthermore, the user can save the current simulation by marking the `Save Simulation` checkbox. Naturally, the user can opt for visualizing a previous simulation by selecting an entry from the dropdown menu.


### Database #Carmen

The application is connected to a MongoDB database, which is a NoSQL database that stores data in flexible, JSON format documents. This component is utilized for storing historical data, represented by previously computed simulations. 

The backend orchestrates the interaction with the database, which includes:
- Saving the current simulation to the database if the user requests it.
- Querying the database for historical data and serving it to the frontend.

Each individual simulation is stored as a document containing the suite of computed individual time steps. Each time step is represented as a JSON document with the following structure:
```json
{
    "simulation_id": <simulation_id>,
    "simulation_name": "Simulation 1",
	"time": 9,
    "cells": [
        {
	    	"colour": true,
	    	"ants": [
	    		{
	    			"direction": "South"
	    		},
	    	],
	    	"rowInd" : 2,
	    	"colInd" : 0,
        }
    ]
}
```

Each time step is indexed by the field `simulation_id`, which is a unique auto-generated identifier for a particular simulation. The `simulation_name` field can be specifed by the user through the frontend, otherwise it is given a generic `Simulation <index>` name. The `time` field represents the current time step of the simulation. The `cells` field contains the list of grid modifications compared to the previous time step. Each cell is defined by its position in the grid, marked by the `rowInd` and `colInd` fields, its colour through the `colour` field (`true` for white, `false` for black) and the list of ants, denoted as`ants`, and their directions in the cell.


### Kubernetes cluster 

<!-- Discuss ingress and overall cluster communication -->

### Terraform deployment #Carmen

<!-- Discuss deployment, terrform state -->


## Scalability considerations

### Scalability

<!-- discuss horizontal/vertical scaling -->

### Fault tolerance #Robin

### Data locality awareness #Robin

### Containerization #Carmen

One principle which constitutes an essential building block of the design of the system architecture is containerization. This practice promotes component isolation and encapsulation, which leverages portability and scalable deployment. The core components of the application, composed of the backend, frontend, simulation etc., are encapsulated using Docker. In this manner, each component is self-contained, allowing seamless deployment and scaling across multiple environemnts. 

### Load balancing

### Sharding - #Carmen if we manage to implement it I guess


## Performance evaluation #Robin


## Limitations and future work

<!-- Could discuss challenges as well. -->


## Conclusion

## Contributions?

<!-- We need an evidence metric that everyone contributed equally in order to get 0.5 extra points but idk if it needs to me mentioned in the report. -->
