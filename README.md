# Performance Testing for OpenMRS

[![Run Performance Tests](https://github.com/openmrs/openmrs-contrib-performance-test/actions/workflows/run-tests-on-gh.yml/badge.svg)](https://github.com/openmrs/openmrs-contrib-performance-test/actions/workflows/run-tests-on-gh.yml)

This repository contains performance testing scripts and configurations for OpenMRS using Gatling.

The latest report can be found
at [here](https://o3-performance.openmrs.org/)

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Running the Tests Locally](#running-the-tests-locally)
- [Additional Resources](#additional-resources)

## Introduction

This project aims to facilitate performance testing for the OpenMRS platform. By using Gatling, it provides a scalable
and easy-to-use framework for simulating user load and measuring system performance.

## Getting Started

### Prerequisites

- Ensure you have OpenMRS running locally on port 80. (Use the docker file: [src/test/resources/docker-compose.yml](src/test/resources/docker-compose.yml))
- Java Development Kit (JDK) > 15 installed.
- Apache Maven installed.

### Running the Tests Locally

To run the performance tests locally, follow these steps:

1. Install dependencies (and compile)
   ```bash
   ./mvnw install -DskipTests
   ```
2. Start OpenMRS on port 80. (Use the docker file: [src/test/resources/docker-compose.yml](src/test/resources/docker-compose.yml) It contains demo patients for the test. [Learn more](#generating-demo-patient-data))
   ```bash
   docker compose -f src/test/resources/docker-compose.yml up
   ```
3. Execute the following command in your terminal:

   **Standard** `export SIMULATION_PRESET='standard' && ./mvnw gatling:test` \
   **dev**  `export SIMULATION_PRESET=dev TIER_COUNT=2 TIER_DURATION_MINUTES=1 USER_INCREMENT_PER_TIER=10 && ./mvnw gatling:test`

This command will initiate the performance tests using Gatling and generate a report upon completion.

4. Setup Git Hook for Code Formatting  
   To automatically format code before committing, set up the Git hook:  

   ```sh
   git config core.hooksPath .githooks
   ```

   Or, you can manually format the code using:

   ```bash
    ./mvnw formatter:format
   ```


## Generating Demo Patient Data

Some scenarios require demo patients in the system, such as the "Visit Patient" scenario. There are several ways to achieve this:

1. Create patients within the scenario.
2. Create patients using the API before running tests.
3. Leverage demo data generation.

Option 3 was chosen because it helps generate patients with realistic data. Since this is a time-consuming task, the demo patients were pre-generated and saved as a database backup in the `src/test/resources/dump` directory. When you spin up the Docker container from the `src/test/resources/docker-compose.yml` file, the backup automatically loads into the database. Therefore, you don’t have to worry about generating and importing them manually.

If you wish to regenerate patients with a fresh database, you can do so either manually or automatically in GitHub Actions.

### To Generate Demo Patients Manually:

1. Navigate to the resources directory:
   ```bash
   cd src/test/resources/
   ```
2. Delete the existing database dump file:
   ```bash
   rm dump/dump.sql
   ```
3. Stop and remove the existing Docker container:
   ```bash
   docker-compose down -v
   ```
4. Pull the newest image and spin up the Docker container:
   ```bash
   docker-compose pull
   docker-compose up
   ```
5. Run the `set_demo_patient_count.sh` script to configure the `referencedemodata.createDemoPatientsOnNextStartup` global property. You can edit the patient count as needed:
   ```bash
   sh set_demo_patient_count.sh
   ```
6. Restart the Docker container and wait until all data is generated. Note that this is a time-consuming task:
   ```bash
   docker-compose restart
   ```
7. Export the patient UUIDs to a CSV file. This file will be used in `src/test/java/org/openmrs/performance/scenarios/VisitPatientScenario.java`:
   ```bash
   sh export_patient_uuids.sh
   ```
8. Take a database dump and replace the current one:
   ```bash
   sh export_db_dump.sh
   ```

### To Generate Demo Patients Automatically on GitHub:

The process is automated with the following workflow:

[Generate Demo Patients Workflow](https://github.com/openmrs/openmrs-contrib-performance-test/tree/main/.github/workflows/generate-demo-patients.yml)

1. Visit the workflow page: [Generate Demo Patients Workflow](https://github.com/openmrs/openmrs-contrib-performance-test/actions/workflows/generate-demo-patients.yml).
2. Click the "Run Workflow" button.

This process may take approximately 4 hours to complete and will automatically commit the database dump and patient UUIDs to the repository.



## Additional Resources

- For more detailed information on using Gatling, refer to the [Gatling documentation](https://gatling.io/docs/).
- View a demo report of the performance tests [here](https://omrs-performance-report.surge.sh/).

Feel free to contribute to this project by submitting issues or pull requests. Happy testing!
