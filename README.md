# Performance Testing for OpenMRS

This repository contains performance testing scripts and configurations for OpenMRS using Gatling.

The latest report can be found at [openmrs.github.io/openmrs-contrib-performance-test](https://openmrs.github.io/openmrs-contrib-performance-test/)

## Table of Contents
- [Introduction](#introduction)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Running the Tests Locally](#running-the-tests-locally)
- [Debugging](#debugging)
- [Additional Resources](#additional-resources)

## Introduction

This project aims to facilitate performance testing for the OpenMRS platform. By using Gatling, it provides a scalable and easy-to-use framework for simulating user load and measuring system performance.

## Getting Started

### Prerequisites

- Ensure you have OpenMRS running locally on port 80.
- Java Development Kit (JDK) > 15 installed.
- Apache Maven installed.

### Running the Tests Locally

To run the performance tests locally, follow these steps:

1. Start OpenMRS on port 80.
2. Execute the following command in your terminal:

    > Note: By default the simulation would run for Standard traffic. Please set `LOAD_SIMULATION_TYPE` env variable to `high` or `peak` to simulate other traffic conditions and to `dev` to run with dynamic load conditions
    
    **Standard**
    `export LOAD_SIMULATION_TYPE='standard' && ./mvnw gatling:test` \
    **High** `export LOAD_SIMULATION_TYPE='high' && ./mvnw gatling:test` \
    **Peak** `export LOAD_SIMULATION_TYPE='peak' && ./mvnw gatling:test` \
    **dev**  `export LOAD_SIMULATION_TYPE=dev ACTIVE_USERS=20 DURATION_MINUTES=10 && ./mvnw gatling:test`


This command will initiate the performance tests using Gatling and generate a report upon completion.

### Simulations

> Note: Simulations on git actions are run with `peak` load simulation type. You can change the simulation type by setting the `LOAD_SIMULATION_TYPE` repository environment variable.

| Load Type | Concurrent Users   | Duration           | Initial Ramp Duration |
|-----------|--------------------|--------------------|-----------------------|
| standard  | 40                 | 1 hour             | 60 seconds            |
| high      | 50                 | 1 hour             | 60 seconds            |
| peak      | 70                 | 1 hour             | 60 seconds            |
| dev       | env `ACTIVE_USERS` | env `ACTIVE_USERS` | 60 seconds            |

Currently, the workload is divided between the following roles:

Doctor: 50% of the active users
Clerk: 50% of the active users


## Debugging

Add the following line to the `logback.xml` file in the `src/test/resources` directory to enable debug logging:

```xml
<logger name="io.gatling.http.engine.response" level="DEBUG" />
```

## Additional Resources

- For more detailed information on using Gatling, refer to the [Gatling documentation](https://gatling.io/docs/).
- View a demo report of the performance tests [here](https://omrs-performance-report.surge.sh/).

Feel free to contribute to this project by submitting issues or pull requests. Happy testing!
