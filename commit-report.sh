#!/bin/bash
set -e

# Delete everything except the target directory
shopt -s extglob
rm -rf !("target")

# Identify the directory starting with test-simulation- inside target/gatling/
report=$(find target/gatling -maxdepth 1 -type d -name "openmrsclinic-*" | head -n 1)

echo $report
# Check if the report directory exists
if [ -d "$report" ]; then
  # Copy the directory to the root
  cp -r "$report"/* .

  # Delete the target directory
  rm -rf target

  # Create a CNAME file (for GitHub Pages)
  echo "o3-performance.openmrs.org" > CNAME

  # Add all changes to git
  git add --all

  # Make a commit
  timestamp=$(date +"%Y-%m-%d %H:%M:%S")
  git commit -m "Update report: $timestamp"
else
  echo "No directory found starting with 'openmrsclinic-' in target/gatling/"
  exit 1
fi
