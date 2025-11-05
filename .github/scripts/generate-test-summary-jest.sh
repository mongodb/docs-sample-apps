#!/bin/bash
set -e

# Generate Test Summary from Jest JSON Output
# Usage: ./generate-test-summary-jest.sh <path-to-jest-json-file>

JSON_FILE="${1:-test-results.json}"

echo "## Test Results" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY

# Parse test results from Jest JSON output
if [ -f "$JSON_FILE" ]; then
  # Extract test counts using jq or grep/sed
  # Jest JSON structure: { "numTotalTests": N, "numPassedTests": N, "numFailedTests": N, "numPendingTests": N, ... }
  
  if command -v jq &> /dev/null; then
    # Use jq if available (preferred)
    total_tests=$(jq -r '.numTotalTests // 0' "$JSON_FILE")
    passed=$(jq -r '.numPassedTests // 0' "$JSON_FILE")
    failed=$(jq -r '.numFailedTests // 0' "$JSON_FILE")
    skipped=$(jq -r '.numPendingTests // 0' "$JSON_FILE")
    
    # Extract failed test details
    failed_tests_file=$(mktemp)
    jq -r '.testResults[]? | select(.status == "failed") | .assertionResults[]? | select(.status == "failed") | "\(.ancestorTitles | join(" > ")) > \(.title)"' "$JSON_FILE" > "$failed_tests_file" 2>/dev/null || true
  else
    # Fallback to grep/sed if jq is not available
    total_tests=$(grep -oP '"numTotalTests":\s*\K[0-9]+' "$JSON_FILE" | head -1)
    passed=$(grep -oP '"numPassedTests":\s*\K[0-9]+' "$JSON_FILE" | head -1)
    failed=$(grep -oP '"numFailedTests":\s*\K[0-9]+' "$JSON_FILE" | head -1)
    skipped=$(grep -oP '"numPendingTests":\s*\K[0-9]+' "$JSON_FILE" | head -1)
    
    # Extract failed test names (basic extraction without jq)
    failed_tests_file=$(mktemp)
    grep -oP '"fullName":\s*"\K[^"]*' "$JSON_FILE" | while read -r line; do
      if echo "$line" | grep -q "failed"; then
        echo "$line" >> "$failed_tests_file"
      fi
    done 2>/dev/null || true
  fi
  
  # Default to 0 if values are empty
  total_tests=${total_tests:-0}
  passed=${passed:-0}
  failed=${failed:-0}
  skipped=${skipped:-0}

  echo "| Status | Count |" >> $GITHUB_STEP_SUMMARY
  echo "|--------|-------|" >> $GITHUB_STEP_SUMMARY
  echo "| ✅ Passed | $passed |" >> $GITHUB_STEP_SUMMARY
  echo "| ❌ Failed | $failed |" >> $GITHUB_STEP_SUMMARY
  echo "| ⏭️ Skipped | $skipped |" >> $GITHUB_STEP_SUMMARY
  echo "| **Total** | **$total_tests** |" >> $GITHUB_STEP_SUMMARY
  echo "" >> $GITHUB_STEP_SUMMARY

  # List failed tests if any
  if [ "$failed" -gt 0 ]; then
    echo "### ❌ Failed Tests" >> $GITHUB_STEP_SUMMARY
    echo "" >> $GITHUB_STEP_SUMMARY
    
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
  
  rm -f "$failed_tests_file"
else
  echo "⚠️ No test results found at: $JSON_FILE" >> $GITHUB_STEP_SUMMARY
  exit 1
fi

