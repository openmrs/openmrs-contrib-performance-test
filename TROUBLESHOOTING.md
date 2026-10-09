# Troubleshooting Guide for OpenMRS Performance Tests

This guide helps you resolve common issues when setting up and running performance tests for OpenMRS.

## Table of Contents

- [OpenMRS Connection Issues](#openmrs-connection-issues)
- [Docker Setup Problems](#docker-setup-problems)
- [Gatling Test Failures](#gatling-test-failures)
- [Maven Build Issues](#maven-build-issues)
- [Performance Test Results](#performance-test-results)

## OpenMRS Connection Issues

### Problem: "Connection refused" or "Cannot reach OpenMRS on port 80"

**Symptoms:**
- Tests fail immediately with connection errors
- Error message: `Connection refused (Connection refused)` or `java.net.ConnectException`

**Solutions:**

1. **Verify OpenMRS is running:**
   ```bash
   # Check if OpenMRS container is running
   docker ps | grep openmrs
   
   # If not running, start the docker container
   docker-compose -f src/test/resources/docker-compose.yml up
   ```

2. **Check if port 80 is accessible:**
   ```bash
   # Try to access OpenMRS directly
   curl http://localhost/openmrs
   
   # If port 80 is in use by another process, check with
   lsof -i :80
   ```

3. **Verify network connectivity:**
   - Ensure your machine can reach `localhost` on port 80
   - If using Docker Desktop, ensure it's running and properly configured
   - On Mac/Windows with Docker Desktop, sometimes port forwarding requires explicit configuration

4. **Wait for OpenMRS to start:**
   - OpenMRS can take 2-5 minutes to fully start after Docker container launch
   - Check logs with: `docker logs <container_id>`
   - Look for message: "OpenMRS has finished startup"

## Docker Setup Problems

### Problem: "docker: command not found"

**Solution:**
1. Install Docker from https://docs.docker.com/get-docker/
2. Verify installation: `docker --version`
3. On Linux, add your user to docker group: `sudo usermod -aG docker $USER`

### Problem: "Insufficient memory" or "Docker container exits unexpectedly"

**Solutions:**
1. Increase Docker memory allocation:
   - Docker Desktop (Mac/Windows): Preferences → Resources → Memory (set to at least 4GB)
2. Check available disk space: `docker system df`
3. Clean up unused images: `docker image prune`
4. Check container logs: `docker logs <container_id>`

## Gatling Test Failures

### Problem: "No simulations found"

**Symptoms:**
- Error when running `./mvnw gatling:test`
- Message: "No simulations found"

**Solutions:**

1. **Verify simulation class exists:**
   ```bash
   # Check for OpenMRSClinic or other simulation classes
   find src/test/java -name '*Simulation.java' -o -name '*Clinic.java'
   ```

2. **Check SIMULATION_PRESET value:**
   ```bash
   # Must be one of: standard, commit, pull_request, or dev
   echo $SIMULATION_PRESET
   
   # Set if not defined
   export SIMULATION_PRESET='dev'
   ```

3. **Rebuild project:**
   ```bash
   ./mvnw clean install -DskipTests
   ```

## Maven Build Issues

### Problem: "mvnw: command not found"

**Solution:**
```bash
# Make sure you're in the correct directory
cd openmrs-contrib-performance-test

# Make script executable
chmod +x mvnw

# Try again
./mvnw --version
```

### Problem: "Could not find artifact" or dependency errors

**Solutions:**

1. **Clear Maven cache:**
   ```bash
   rm -rf ~/.m2/repository
   ./mvnw clean install
   ```

2. **Check internet connection:**
   - Maven needs to download dependencies
   - Ensure firewall isn't blocking artifact repositories

## Performance Test Results

### Problem: "Report generation failed" or "Cannot find results"

**Solutions:**

1. **Check test output directory:**
   ```bash
   # Reports are typically in target/gatling/
   ls -la target/gatling/
   ```

2. **Open test report:**
   ```bash
   # Find the latest simulation result
   ls -t target/gatling/ | head -1
   
   # Open index.html in browser
   open target/gatling/[simulation-name]/index.html
   ```

## Getting Help

If you still encounter issues:

1. **Check existing issues:** https://github.com/openmrs/openmrs-contrib-performance-test/issues
2. **Review logs:**
   - Docker logs: `docker logs <container_id>`
   - Gatling logs: `target/gatling/*/simulation.log`
3. **Ask on OpenMRS Community:** https://talk.openmrs.org/
