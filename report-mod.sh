#!/bin/bash

set -e

BASE_GATLING_DIR="target/gatling"

REPORT_PATTERN="openmrsclinic-*"

CUSTOM_JS="CustomReportMod/CustomiseReport.js"
CUSTOM_CSS="CustomReportMod/style.css"

REPORT_DIR=$(find "$BASE_GATLING_DIR" -maxdepth 1 -type d -name "$REPORT_PATTERN" | head -n 1)

if [ -z "$REPORT_DIR" ]; then
    echo "❌ Error: Could not find a Gatling report directory."
    echo "Please check the BASE_GATLING_DIR and REPORT_PATTERN variables."
    exit 1
fi

TARGET_JS="$REPORT_DIR/js/gatling.js"
TARGET_CSS="$REPORT_DIR/style/style.css"

if [ ! -f "$TARGET_JS" ]; then
    echo "❌ Error: Target file not found: $TARGET_JS"
    exit 1
fi

if grep -q "GATLING-REPORT-MOD-JS-START" "$TARGET_JS"; then
    echo "🟡 JS modification already exists in $TARGET_JS. Skipping."
else
    echo "" >> "$TARGET_JS"
    cat "$CUSTOM_JS" >> "$TARGET_JS"
    echo "✔️ Successfully modified JavaScript."
fi

if [ ! -f "$TARGET_CSS" ]; then
    echo "❌ Error: Target file not found: $TARGET_CSS"
    exit 1
fi

if grep -q "GATLING-REPORT-MOD-CSS-START" "$TARGET_CSS"; then
    echo "🟡 CSS modification already exists in $TARGET_CSS. Skipping."
else
    echo "" >> "$TARGET_CSS"
    cat "$CUSTOM_CSS" >> "$TARGET_CSS"
    echo "✔️ Successfully modified CSS."
fi
