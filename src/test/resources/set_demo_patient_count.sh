#!/bin/bash

# MySQL credentials
DB_USER="openmrs"
DB_PASS="openmrs"
DB_NAME="openmrs"
DB_PORT="3306"
DB_HOST="127.0.0.1"
PATIENT_COUNT=200

# Query to be executed
QUERY="UPDATE global_property SET property_value=$PATIENT_COUNT WHERE property='referencedemodata.createDemoPatientsOnNextStartup';"

# Inform about the process and count
echo "Running SQL query to update 'referencedemodata.createDemoPatientsOnNextStartup' property_value to $PATIENT_COUNT"
echo "Expected patient count: $PATIENT_COUNT"

# Execute the SQL query
result=$(mysql -u$DB_USER -p$DB_PASS -D$DB_NAME -P$DB_PORT -h$DB_HOST -e "$QUERY" 2>&1)

# Print the result of the query execution
if [ $? -eq 0 ]; then
    echo "Query executed successfully."
else
    echo "Error executing query: $result"
fi
