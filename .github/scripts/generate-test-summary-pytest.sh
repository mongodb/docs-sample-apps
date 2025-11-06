#!/bin/bash
set -e

# Generate Test Summary from Pytest JUnit XML Output
# Usage: ./generate-test-summary-pytest.sh <path-to-junit-xml>

XML_FILE="${1:-test-results.xml}"

echo "## Test Results" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY

# Parse test results from JUnit XML
if [ -f "$XML_FILE" ]; then
  # Extract test counts from XML
  # JUnit XML structure: <testsuite tests="N" failures="N" errors="N" skipped="N">
  
  tests=$(grep -oP 'tests="\K[0-9]+' "$XML_FILE" | head -1)
  failures=$(grep -oP 'failures="\K[0-9]+' "$XML_FILE" | head -1)
  errors=$(grep -oP 'errors="\K[0-9]+' "$XML_FILE" | head -1)
  skipped=$(grep -oP 'skipped="\K[0-9]+' "$XML_FILE" | head -1)
  
  # Default to 0 if values are empty
  tests=${tests:-0}
  failures=${failures:-0}
  errors=${errors:-0}
  skipped=${skipped:-0}
  
  passed=$((tests - failures - errors - skipped))
  
  echo "| Status | Count |" >> $GITHUB_STEP_SUMMARY
  echo "|--------|-------|" >> $GITHUB_STEP_SUMMARY
  echo "| ✅ Passed | $passed |" >> $GITHUB_STEP_SUMMARY
  echo "| ❌ Failed | $((failures + errors)) |" >> $GITHUB_STEP_SUMMARY
  echo "| ⏭️ Skipped | $skipped |" >> $GITHUB_STEP_SUMMARY
  echo "| **Total** | **$tests** |" >> $GITHUB_STEP_SUMMARY
  echo "" >> $GITHUB_STEP_SUMMARY
  
  # List failed tests if any
  if [ $((failures + errors)) -gt 0 ]; then
    echo "### ❌ Failed Tests" >> $GITHUB_STEP_SUMMARY
    echo "" >> $GITHUB_STEP_SUMMARY
    
    # Extract failed test names from XML
    failed_tests_file=$(mktemp)
    
    # Find testcase elements with failure or error children
    grep -oP '<testcase[^>]*classname="[^"]*"[^>]*name="[^"]*"[^>]*>.*?<(failure|error)' "$XML_FILE" | \
      grep -oP 'classname="\K[^"]*|name="\K[^"]*' | \
      paste -d '.' - - >> "$failed_tests_file" 2>/dev/null || true
    
    if [ -s "$failed_tests_file" ]; then
      while IFS= read -r test; do
        echo "- \`$test\`" >> $GITHUB_STEP_SUMMARY
      done < "$failed_tests_file"
    else
      echo "_Unable to parse individual test names_" >> $GITHUB_STEP_SUMMARY
    fi
    
    echo "" >> $GITHUB_STEP_SUMMARY
    echo "❌ **Tests failed!**" >> $GITHUB_STEP_SUMMARY
    rm -f "$failed_tests_file"
    exit 1
  else
    echo "✅ **All tests passed!**" >> $GITHUB_STEP_SUMMARY
  fi
else
  echo "⚠️ No test results found at: $XML_FILE" >> $GITHUB_STEP_SUMMARY
  exit 1
fi

