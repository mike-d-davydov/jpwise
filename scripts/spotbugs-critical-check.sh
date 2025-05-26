#!/bin/bash

# SpotBugs Critical Issues Check Script
# This script runs SpotBugs and only fails on critical (Priority 1) issues

set -e

echo "Running SpotBugs analysis..."

# Run SpotBugs to generate the report (don't fail on this)
# Note: SpotBugs should already be run by the CI, this is just a fallback
if [ ! -f "target/spotbugsXml.xml" ]; then
    echo "SpotBugs report not found, running SpotBugs..."
    mvn spotbugs:spotbugs
else
    echo "SpotBugs report already exists, using existing report."
fi

# Check if the report was generated
if [ ! -f "target/spotbugsXml.xml" ]; then
    echo "SpotBugs report not found. Analysis may have failed."
    exit 1
fi

# Count critical issues (Priority 1)
CRITICAL_ISSUES=$(grep -c 'priority="1"' target/spotbugsXml.xml 2>/dev/null || echo "0")

# Ensure CRITICAL_ISSUES is numeric
if ! [[ "$CRITICAL_ISSUES" =~ ^[0-9]+$ ]]; then
    CRITICAL_ISSUES=0
fi

echo "SpotBugs analysis completed."
echo "Critical issues found: $CRITICAL_ISSUES"

if [ "$CRITICAL_ISSUES" -gt 0 ]; then
    echo ""
    echo "❌ CRITICAL ISSUES FOUND!"
    echo "The following critical issues must be fixed:"
    echo ""
    
    # Extract and display critical issues
    grep -A 5 -B 1 'priority="1"' target/spotbugsXml.xml | grep -E '(type=|class=|method=)' | head -20
    
    echo ""
    echo "Full report available at: target/spotbugsXml.xml"
    echo "HTML report available at: target/spotbugs.html"
    echo ""
    echo "Please fix these critical issues before committing."
    exit 1
else
    echo "✅ No critical issues found. Build can proceed."
    
    # Show summary of all issues for information
    TOTAL_ISSUES=$(grep -c '<BugInstance' target/spotbugsXml.xml 2>/dev/null || echo "0")
    
    # Ensure TOTAL_ISSUES is numeric
    if ! [[ "$TOTAL_ISSUES" =~ ^[0-9]+$ ]]; then
        TOTAL_ISSUES=0
    fi
    
    if [ "$TOTAL_ISSUES" -gt 0 ]; then
        echo "ℹ️  Total issues found (all priorities): $TOTAL_ISSUES"
        echo "   These are informational and won't fail the build."
    fi
    
    exit 0
fi 