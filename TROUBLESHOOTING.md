# Troubleshooting Guide

This document covers common setup and execution issues encountered while configuring and running the OpenMRS performance test project.

---

# 1. Maven PATH Issues (`mvn` / `mvnd` Not Recognized)

## Problem

Terminal commands such as `mvn` or `mvnd` are not recognized.

---

## Symptoms

```text
'mvn' is not recognized as an internal or external command
```

or

```text
'mvnd' is not recognized as an internal or external command
```

---

## Cause

Maven is either:

- not installed correctly
- or its `bin` directory is missing from the system `Path`

---

## Solution

Verify Maven installation and add the Maven `bin` directory to the system environment variables.

Example:

```text
C:\Program Files\Apache\Maven\bin
```

After updating environment variables:

1. Close PowerShell or Command Prompt
2. Open a new terminal window
3. Verify installation:

```bash
mvn -v
```

or

```bash
mvnd -v
```

---

# 2. `JAVA_HOME` Configuration Warnings

## Problem

Maven reports Java configuration warnings even when Java is installed.

---

## Symptoms

```text
JAVA_HOME environment variable is not defined correctly
```

---

## Cause

Some Maven plugins rely on the `JAVA_HOME` environment variable instead of using Java directly from the system `Path`.

---

## Solution

Create a `JAVA_HOME` system variable pointing to the installed JDK directory.

Example:

```text
C:\Program Files\Eclipse Adoptium\jdk-17
```

Also add Java to the system `Path`:

```text
%JAVA_HOME%\bin
```

Restart the terminal after making changes.

---

# 3. Separate OpenMRS Instance Requirement

## Problem

Running Docker commands inside this repository fails.

---

## Symptoms

```text
docker-compose.yml not found
```

---

## Cause

This repository contains performance test scripts only and does not include a standalone OpenMRS runtime environment.

---

## Solution

Before running performance tests, start a separate OpenMRS instance using one of the following:

- OpenMRS SDK
- Standalone OpenMRS Docker setup
- Existing local or remote OpenMRS server

Verify the instance is accessible before running tests.

Example:

```bash
curl http://localhost:8080/openmrs/ws/rest/v1/session
```

---

# 4. Docker Resource and Connection Issues

## Problem

Performance tests run slowly or containers become unstable.

---

## Symptoms

- Requests taking more than 100 seconds
- Containers exiting with code `137`
- Connection failures during simulations

---

## Cause

Performance testing requires additional CPU and memory resources.

---

## Solution

Increase Docker Desktop resource allocation.

Recommended minimum:

- 4 GB RAM
- 2 CPUs

---

## Reset Docker Environment

If the environment becomes unstable:

```bash
docker-compose down -v
docker-compose up -d
```

---

## Inspect Logs

Monitor backend logs during test execution:

```bash
docker logs -f <container-name>
```

---

# 5. Gatling Assertion Failures and Endpoint Debugging

## Problem

Gatling simulations fail with assertion errors.

---

## Symptoms

- Maven reports `BUILD FAILURE`
- Gatling output shows failed requests
- Multiple requests marked as `KO`

Example:

```text
status.find.in([200, 209], 304), found 404
```

---

## Cause

These failures are commonly caused by:

- invalid endpoints
- incorrect OpenMRS version compatibility
- backend connection issues

---

## Solution

Review the `Errors` section in the Gatling output to identify failing requests.

Manually test the endpoint:

```bash
curl http://localhost:8080/openmrs/ws/rest/v1/session
```

Verify the configured `baseUrl` matches the running OpenMRS instance.

Example:

```conf
baseUrl = "http://localhost:8080/openmrs"
```

---

## Clean Maven Build

If dependency or execution issues occur:

```bash
mvn clean install -U
```

---

# 6. `ENV_SIMULATION_PRESET` Setup

## Problem

The simulation preset environment variable is missing.

---

## Symptoms

```text
IllegalArgumentException: ENV_SIMULATION_PRESET variable is not set
```

---

## Cause

The Gatling simulation requires a preset configuration before execution.

---

## Solution

Set the environment variable before running tests.

Example:

```bash
set ENV_SIMULATION_PRESET=small
```

Possible preset values:

- `small`
- `medium`
- `large`

---

# 7. Windows-Specific `mvnd` Whitespace Path Issues

## Problem

`mvnd` fails with `ForkException` errors on Windows.

---

## Symptoms

- Maven Daemon crashes unexpectedly
- JVM startup errors
- `ForkException` during simulation execution

---

## Cause

Some JDK installation paths contain whitespace, which may cause path parsing issues with Maven Daemon (`mvnd`).

Example:

```text
C:\Program Files\Eclipse Adoptium\
```

---

## Solutions

### Option 1: Use Standard Maven

Use `mvn` instead of `mvnd`.

---

### Option 2: Pass JVM Arguments Explicitly

```bash
mvnd gatling:test "-Dgatling.jvmArgs=-DENV_SIMULATION_PRESET=small"
```

---

### Option 3: Install JDK in a Path Without Spaces

Example:

```text
C:\Java\jdk-17
```

---

# 8. Additional Help

## OpenMRS Talk

https://talk.openmrs.org/

---

## OpenMRS Issue Tracker

https://issues.openmrs.org/

---

## OpenMRS Documentation

https://openmrs.atlassian.net/wiki/