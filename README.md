# Performance Testing for OpenMRS

This repository contains performance testing scripts and configurations for OpenMRS using Gatling.


The latest report can be found
at [openmrs.github.io/openmrs-contrib-performance-test](https://openmrs.github.io/openmrs-contrib-performance-test/)

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Running the Tests Locally](#running-the-tests-locally)
- [Debugging](#debugging)
- [Additional Resources](#additional-resources)

## Introduction

This project aims to facilitate performance testing for the OpenMRS platform. By using Gatling, it provides a scalable
and easy-to-use framework for simulating user load and measuring system performance.

## Getting Started

### Prerequisites

- Ensure you have OpenMRS running locally on port 80.
- Java Development Kit (JDK) > 15 installed.
- Apache Maven installed.

### Running the Tests Locally

To run the performance tests locally, follow these steps:

1. Start OpenMRS on port 80.
2. Execute the following command in your terminal:

   **Standard** `export SIMULATION_PRESET='standard' && ./mvnw gatling:test` \
   **dev
   **  `export SIMULATION_PRESET=dev TIER_COUNT=2 TIER_DURATION_MINUTES=1 USER_INCREMENT_PER_TIER=10 && ./mvnw gatling:test`

This command will initiate the performance tests using Gatling and generate a report upon completion.

### Simulation Presets

Simulation presets are configured within the OpenMRSClinic class. Below are the available presets. To add a new
simulation, define it in the OpenMRSClinic class.

During test runs, each simulation begins with 0 users and gradually increases by the specified user increment per tier
for one minute. It then runs with a constant number of users for the tier duration before ramping up again for one
minute to the next tier. This process continues until the final tier is reached.

| Preset       | Tier Count       | Tier Duration               | User increment per tier       | Ramp duration |
|--------------|------------------|-----------------------------|-------------------------------|---------------|
| standard     | 6                | 30 min                      | 32                            | 1 min         |
| commit       | 1                | 1 min                       | 20                            | 1 min         |
| pull_request | 1                | 1 min                       | 20                            | 1 min         |
| dev          | env `TIER_COUNT` | env `TIER_DURATION_MINUTES` | env `USER_INCREMENT_PER_TIER` | 1 min         |

In GitHub Actions, the commit and pull_request presets are used for commits and pull requests, respectively. The
standard preset is used for scheduled runs, which occur daily, updating the report at [o3-performance.openmrs.org](https://o3-performance.openmrs.org).

Currently, the workload is divided between the following Personas:

- Doctor: 50% of the active users
- Clerk: 50% of the active users

## Debugging

Add the following line to the `logback.xml` file in the `src/test/resources` directory to enable debug logging:

```xml

<logger name="io.gatling.http.engine.response" level="DEBUG"/>
```

## Additional Resources

- For more detailed information on using Gatling, refer to the [Gatling documentation](https://gatling.io/docs/).
- View a demo report of the performance tests [here](https://omrs-performance-report.surge.sh/).

Feel free to contribute to this project by submitting issues or pull requests. Happy testing!
