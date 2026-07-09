# Troubleshooting Guide

## 1. OpenMRS connection issues (Port 80)
If OpenMRS is not accessible:
- Check if port 80 is already in use
- Stop the conflicting process
- Or change the port

## 2. Docker issues
If Docker is not working:
- Ensure Docker is installed
- Start Docker before running the project
- Check Docker permissions

## 3. Maven build issues
If build fails:
- Run: mvn clean install
- Check Java version compatibility

## 4. Gatling test failures
If tests fail:
- Check logs for errors
- Ensure all dependencies are installed
