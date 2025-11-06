#!/bin/bash
set -e

# Generate Test Summary from Maven Surefire Reports
# Usage: ./generate-test-summary-surefire.sh <path-to-surefire-reports>

REPORTS_DIR="${1:-target/surefire-reports}"

echo "## Test Results" >> $GITHUB_STEP_SUMMARY
echo "" >> $GITHUB_STEP_SUMMARY

# Parse test results from Surefire reports
if [ -d "$REPORTS_DIR" ]; then
  total_tests=0
  failures=0
  errors=0
  skipped=0
  failed_tests_file=$(mktemp)

  for file in "$REPORTS_DIR"/TEST-*.xml; do
    if [ -f "$file" ]; then
      # Extract test counts from XML
      tests=$(grep -oP 'tests="\K[0-9]+' "$file" | head -1)
      fails=$(grep -oP 'failures="\K[0-9]+' "$file" | head -1)
      errs=$(grep -oP 'errors="\K[0-9]+' "$file" | head -1)
      skip=$(grep -oP 'skipped="\K[0-9]+' "$file" | head -1)

      total_tests=$((total_tests + ${tests:-0}))
      failures=$((failures + ${fails:-0}))
      errors=$((errors + ${errs:-0}))
      skipped=$((skipped + ${skip:-0}))

      # Extract failed test cases
      if [ "${fails:-0}" -gt 0 ] || [ "${errs:-0}" -gt 0 ]; then
        classname=$(basename "$file" .xml | sed 's/^TEST-//')

        # Find failed testcases (with failure or error elements)
        grep -oP '<testcase[^>]*name="[^"]*"[^>]*>.*?<(failure|error)' "$file" | \
          grep -oP 'name="\K[^"]*' | while read -r testname; do
            echo "$classname.$testname" >> "$failed_tests_file"
          done
      fi
    fi
  done

  passed=$((total_tests - failures - errors - skipped))

  echo "| Status | Count |" >> $GITHUB_STEP_SUMMARY
  echo "|--------|-------|" >> $GITHUB_STEP_SUMMARY
  echo "| ✅ Passed | $passed |" >> $GITHUB_STEP_SUMMARY
  echo "| ❌ Failed | $((failures + errors)) |" >> $GITHUB_STEP_SUMMARY
  echo "| ⏭️ Skipped | $skipped |" >> $GITHUB_STEP_SUMMARY
  echo "| **Total** | **$total_tests** |" >> $GITHUB_STEP_SUMMARY
  echo "" >> $GITHUB_STEP_SUMMARY

  # List failed tests if any
  if [ $((failures + errors)) -gt 0 ]; then
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
  echo "⚠️ No test results found at: $REPORTS_DIR" >> $GITHUB_STEP_SUMMARY
  exit 1
fi

