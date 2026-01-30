#!/bin/bash
set -e

# Generate Detailed Test Summary from Multiple Jest JSON Output Files
# Shows breakdown by test type (unit vs integration)
# Usage: ./generate-test-summary-jest.sh <unit-json> <integration-json>

# Guard: skip if GITHUB_STEP_SUMMARY is not set
if [ -z "$GITHUB_STEP_SUMMARY" ]; then
  echo "Warning: GITHUB_STEP_SUMMARY not set, skipping summary generation"
  exit 0
fi

UNIT_JSON="${1:-}"
INTEGRATION_JSON="${2:-}"

echo "## Test Results" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# Function to parse Jest JSON file
parse_json() {
  local json_file="$1"
  local test_type="$2"

  if [ ! -f "$json_file" ]; then
    echo "0 0 0 0"
    return
  fi

  if command -v jq &> /dev/null; then
    # Use jq if available (preferred)
    total_tests=$(jq -r '.numTotalTests // 0' "$json_file")
    passed=$(jq -r '.numPassedTests // 0' "$json_file")
    failed=$(jq -r '.numFailedTests // 0' "$json_file")
    skipped=$(jq -r '.numPendingTests // 0' "$json_file")
  else
    # Fallback to grep/sed if jq is not available
    total_tests=$(grep -oP '"numTotalTests":\s*\K[0-9]+' "$json_file" | head -1)
    passed=$(grep -oP '"numPassedTests":\s*\K[0-9]+' "$json_file" | head -1)
    failed=$(grep -oP '"numFailedTests":\s*\K[0-9]+' "$json_file" | head -1)
    skipped=$(grep -oP '"numPendingTests":\s*\K[0-9]+' "$json_file" | head -1)
  fi

  # Default to 0 if values are empty
  total_tests=${total_tests:-0}
  passed=${passed:-0}
  failed=${failed:-0}
  skipped=${skipped:-0}

  echo "$total_tests $passed $failed $skipped"
}

# Parse both files
read -r unit_tests unit_passed unit_failed unit_skipped <<< "$(parse_json "$UNIT_JSON" "Unit")"
read -r int_tests int_passed int_failed int_skipped <<< "$(parse_json "$INTEGRATION_JSON" "Integration")"

# Calculate totals
total_tests=$((unit_tests + int_tests))
total_passed=$((unit_passed + int_passed))
total_failed=$((unit_failed + int_failed))
total_skipped=$((unit_skipped + int_skipped))

# Display detailed breakdown
echo "### Summary by Test Type" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"
echo "| Test Type | Passed | Failed | Skipped | Total |" >> "$GITHUB_STEP_SUMMARY"
echo "|-----------|--------|--------|---------|-------|" >> "$GITHUB_STEP_SUMMARY"

if [ -f "$UNIT_JSON" ]; then
  echo "| 🔧 Unit Tests | $unit_passed | $unit_failed | $unit_skipped | $unit_tests |" >> "$GITHUB_STEP_SUMMARY"
fi

if [ -f "$INTEGRATION_JSON" ]; then
  echo "| 🔗 Integration Tests | $int_passed | $int_failed | $int_skipped | $int_tests |" >> "$GITHUB_STEP_SUMMARY"
fi

echo "| **Total** | **$total_passed** | **$total_failed** | **$total_skipped** | **$total_tests** |" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# Overall status
echo "### Overall Status" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"
echo "| Status | Count |" >> "$GITHUB_STEP_SUMMARY"
echo "|--------|-------|" >> "$GITHUB_STEP_SUMMARY"
echo "| ✅ Passed | $total_passed |" >> "$GITHUB_STEP_SUMMARY"
echo "| ❌ Failed | $total_failed |" >> "$GITHUB_STEP_SUMMARY"
echo "| ⏭️ Skipped | $total_skipped |" >> "$GITHUB_STEP_SUMMARY"
echo "| **Total** | **$total_tests** |" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# List failed tests if any
if [ $total_failed -gt 0 ]; then
  echo "### ❌ Failed Tests" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"

  failed_tests_file=$(mktemp)

  # Extract failed tests from both files
  for json_file in "$UNIT_JSON" "$INTEGRATION_JSON"; do
    if [ -f "$json_file" ]; then
      if command -v jq &> /dev/null; then
        jq -r '.testResults[]? | select(.status == "failed") | .assertionResults[]? | select(.status == "failed") | "\(.ancestorTitles | join(" > ")) > \(.title)"' "$json_file" >> "$failed_tests_file" 2>/dev/null || true
      else
        # Basic fallback without jq
        grep -oP '"fullName":\s*"\K[^"]*' "$json_file" | while read -r line; do
          if echo "$line" | grep -q "failed"; then
            echo "$line" >> "$failed_tests_file"
          fi
        done 2>/dev/null || true
      fi
    fi
  done

  if [ -s "$failed_tests_file" ]; then
    while IFS= read -r test; do
      echo "- \`$test\`" >> "$GITHUB_STEP_SUMMARY"
    done < "$failed_tests_file"
  else
    echo "_Unable to parse individual test names_" >> "$GITHUB_STEP_SUMMARY"
  fi

  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "❌ **Tests failed!**" >> "$GITHUB_STEP_SUMMARY"
  rm -f "$failed_tests_file"
  exit 1
else
  echo "✅ **All tests passed!**" >> "$GITHUB_STEP_SUMMARY"
fi
