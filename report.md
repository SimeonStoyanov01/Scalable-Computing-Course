# Scalable computing - Report

## Introduction


## Project Overview

<!-- Explain the general outline of the project and how we tackled it -->

<!-- Explain core functionality; what can be done through our app -->

## Data pipelines

### Streamline data #Robin?

<!-- Include pipeline figure if you can -->

### Historical data #Carmen

<!-- Include pipeline figure if you can -->


## Infrastructure Setup

### Simulation - Spark #Robin

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

### Load balancing

### Sharding - #Carmen if we manage to implement it I guess


## Performance evaluation #Robin


## Limitations and future work

<!-- Could discuss challenges as well. -->


## Conclusion

## Contributions?

<!-- We need an evidence metric that everyone contributed equally in order to get 0.5 extra points but idk if it needs to me mentioned in the report. -->
