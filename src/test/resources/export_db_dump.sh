#!/bin/bash

# MySQL credentials
DB_USER="openmrs"
DB_PASS="openmrs"
DB_NAME="openmrs"
DB_PORT="3306"
DB_HOST="127.0.0.1"

# Directory to save the dump
DUMP_DIR="./dump"
DUMP_FILE="${DUMP_DIR}/dump.sql"

# Take the database dump
echo "Taking a backup of the database '$DB_NAME' and saving it in '$DUMP_DIR'..."

mysqldump -u$DB_USER -p$DB_PASS -h$DB_HOST -P$DB_PORT $DB_NAME > "$DUMP_FILE" 2>/dev/null

# Check if the dump was successful
if [ $? -eq 0 ]; then
    echo "Backup successful. Dump file: $DUMP_FILE"
else
    echo "Backup failed. Please check your MySQL credentials or connection."
fi
