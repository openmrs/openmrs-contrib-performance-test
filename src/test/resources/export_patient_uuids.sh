#!/bin/bash

# MySQL credentials
DB_USER="openmrs"
DB_PASS="openmrs"
DB_NAME="openmrs"
DB_PORT="3306"
DB_HOST="127.0.0.1"

# Query to be executed
QUERY="SELECT uuid AS patient_uuid FROM patient INNER JOIN person ON patient.patient_id = person.person_id;"

# Output CSV file
OUTPUT_FILE="patient_uuids.csv"

mysql -u$DB_USER -p$DB_PASS -D$DB_NAME -P$DB_PORT -h$DB_HOST -e "$QUERY" | sed 's/\t/,/g' > $OUTPUT_FILE

echo "Query results have been exported to $OUTPUT_FILE"
