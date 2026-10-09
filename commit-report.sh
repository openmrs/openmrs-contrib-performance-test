#!/bin/bash
set -e

# Delete everything except the target directory
shopt -s extglob

# Preserve target, performance-trends, and response-sizes
rm -rf !(target|performance-trends|response-sizes)

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

  # Ensure response_sizes.csv stays well under GitHub's 100MB file limit
  if [ -f "response-sizes/response_sizes.csv" ]; then
    max_bytes=$((50 * 1024 * 1024))
    file_size=$(wc -c < "response-sizes/response_sizes.csv" 2>/dev/null || echo 0)
    if [ "$file_size" -gt "$max_bytes" ]; then
      echo "response_sizes.csv ($file_size bytes) exceeds limit. Truncating to safe size..."
      header=$(head -n 1 "response-sizes/response_sizes.csv")
      tail -c $((45 * 1024 * 1024)) "response-sizes/response_sizes.csv" | sed '1d' > "response-sizes/response_sizes.tmp"
      echo "$header" > "response-sizes/response_sizes.csv"
      cat "response-sizes/response_sizes.tmp" >> "response-sizes/response_sizes.csv"
      rm -f "response-sizes/response_sizes.tmp"
    fi
  fi

  # Add all changes to git
  git add --all
  if [ -d "response-sizes" ]; then
    git add -f response-sizes/
  fi

  # Make a commit
  timestamp=$(date +"%Y-%m-%d %H:%M:%S")
  git commit -m "Update report: $timestamp"
else
  echo "No directory found starting with 'openmrsclinic-' in target/gatling/"
  exit 1
fi
