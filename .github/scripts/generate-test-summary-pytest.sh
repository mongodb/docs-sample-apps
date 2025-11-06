#!/bin/bash
set -e

# Generate Detailed Test Summary from Multiple Pytest JUnit XML Output Files
# Shows breakdown by test type (unit vs integration)
# Usage: ./generate-test-summary-pytest-detailed.sh <unit-xml> <integration-xml>

UNIT_XML="${1:-}"
INTEGRATION_XML="${2:-}"

echo "## Test Results" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY

# Function to parse XML file
parse_xml() {
  local xml_file="$1"
  local test_type="$2"
  
  if [ ! -f "$xml_file" ]; then
    echo "0 0 0 0 0"
    return
  fi
  
  tests=$(grep -oP 'tests="\K[0-9]+' "$xml_file" | head -1)
  failures=$(grep -oP 'failures="\K[0-9]+' "$xml_file" | head -1)
  errors=$(grep -oP 'errors="\K[0-9]+' "$xml_file" | head -1)
  skipped=$(grep -oP 'skipped="\K[0-9]+' "$xml_file" | head -1)
  
  tests=${tests:-0}
  failures=${failures:-0}
  errors=${errors:-0}
  skipped=${skipped:-0}
  passed=$((tests - failures - errors - skipped))
  
  echo "$tests $failures $errors $skipped $passed"
}

# Parse both files
read -r unit_tests unit_failures unit_errors unit_skipped unit_passed <<< "$(parse_xml "$UNIT_XML" "Unit")"
read -r int_tests int_failures int_errors int_skipped int_passed <<< "$(parse_xml "$INTEGRATION_XML" "Integration")"

# Calculate totals
total_tests=$((unit_tests + int_tests))
total_failures=$((unit_failures + int_failures))
total_errors=$((unit_errors + int_errors))
total_skipped=$((unit_skipped + int_skipped))
total_passed=$((unit_passed + int_passed))
total_failed=$((total_failures + total_errors))

# Display detailed breakdown
echo "### Summary by Test Type" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY
echo "| Test Type | Passed | Failed | Skipped | Total |" >> $GITHUB_STEP_SUMMARY
echo "|-----------|--------|--------|---------|-------|" >> $GITHUB_STEP_SUMMARY

if [ -f "$UNIT_XML" ]; then
  echo "| 🔧 Unit Tests | $unit_passed | $((unit_failures + unit_errors)) | $unit_skipped | $unit_tests |" >> $GITHUB_STEP_SUMMARY
fi

if [ -f "$INTEGRATION_XML" ]; then
  echo "| 🔗 Integration Tests | $int_passed | $((int_failures + int_errors)) | $int_skipped | $int_tests |" >> $GITHUB_STEP_SUMMARY
fi

echo "| **Total** | **$total_passed** | **$total_failed** | **$total_skipped** | **$total_tests** |" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY

# Overall status
echo "### Overall Status" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY
echo "| Status | Count |" >> $GITHUB_STEP_SUMMARY
echo "|--------|-------|" >> $GITHUB_STEP_SUMMARY
echo "| ✅ Passed | $total_passed |" >> $GITHUB_STEP_SUMMARY
echo "| ❌ Failed | $total_failed |" >> $GITHUB_STEP_SUMMARY
echo "| ⏭️ Skipped | $total_skipped |" >> $GITHUB_STEP_SUMMARY
echo "| **Total** | **$total_tests** |" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY

# List failed tests if any
if [ $total_failed -gt 0 ]; then
  echo "### ❌ Failed Tests" >> $GITHUB_STEP_SUMMARY
  echo "" >> $GITHUB_STEP_SUMMARY
  
  failed_tests_file=$(mktemp)
  
  # Extract failed tests from both files
  for xml_file in "$UNIT_XML" "$INTEGRATION_XML"; do
    if [ -f "$xml_file" ]; then
      grep -oP '<testcase[^>]*classname="[^"]*"[^>]*name="[^"]*"[^>]*>.*?<(failure|error)' "$xml_file" | \
        grep -oP 'classname="\K[^"]*|name="\K[^"]*' | \
        paste -d '.' - - >> "$failed_tests_file" 2>/dev/null || true
    fi
  done
  
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

