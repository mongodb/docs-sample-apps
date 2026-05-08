#!/bin/bash
set -e

# Generate Security Audit Summary from pip-audit JSON output
# Usage: ./generate-audit-summary-pip.sh <audit-json-file> <project-label>
# Example: ./generate-audit-summary-pip.sh audit-results.json "Python FastAPI"

# Guard: skip if GITHUB_STEP_SUMMARY is not set
if [ -z "$GITHUB_STEP_SUMMARY" ]; then
  echo "Warning: GITHUB_STEP_SUMMARY not set, skipping summary generation"
  exit 0
fi

AUDIT_JSON="${1:-}"
PROJECT_LABEL="${2:-Python project}"

if [ ! -f "$AUDIT_JSON" ]; then
  echo "⚠️ Audit JSON file not found: $AUDIT_JSON" >> "$GITHUB_STEP_SUMMARY"
  exit 1
fi

echo "## 🔒 Security Audit — $PROJECT_LABEL" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# pip-audit JSON structure: { "dependencies": [ { "name": "...", "version": "...", "vulns": [...] } ] }
# Count vulnerable packages (those with non-empty vulns arrays)
total_deps=$(jq '[.dependencies | length] | first // 0' "$AUDIT_JSON")
vuln_packages=$(jq '[.dependencies[] | select(.vulns | length > 0)] | length' "$AUDIT_JSON")
total_vulns=$(jq '[.dependencies[].vulns | length] | add // 0' "$AUDIT_JSON")

# Summary table
echo "### Vulnerability Summary" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"
echo "| Metric | Count |" >> "$GITHUB_STEP_SUMMARY"
echo "|--------|-------|" >> "$GITHUB_STEP_SUMMARY"
echo "| 📦 Total dependencies scanned | $total_deps |" >> "$GITHUB_STEP_SUMMARY"
echo "| ⚠️ Vulnerable packages | $vuln_packages |" >> "$GITHUB_STEP_SUMMARY"
echo "| 🔓 Total vulnerabilities | $total_vulns |" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# If there are vulnerabilities, list them
if [ "$total_vulns" -gt 0 ]; then
  echo "### Vulnerability Details" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "| Package | Version | Vulnerability ID | Fix Versions |" >> "$GITHUB_STEP_SUMMARY"
  echo "|---------|---------|-----------------|--------------|" >> "$GITHUB_STEP_SUMMARY"

  jq -r '
    .dependencies[] |
    select(.vulns | length > 0) |
    . as $dep |
    .vulns[] |
    "| \($dep.name) | \($dep.version) | \(.id) | \(.fix_versions | join(", ") // "N/A") |"
  ' "$AUDIT_JSON" >> "$GITHUB_STEP_SUMMARY" 2>/dev/null || true

  echo "" >> "$GITHUB_STEP_SUMMARY"

  # Action items
  echo "### 🛠️ What to do" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "1. Update the affected packages in \`requirements.in\`" >> "$GITHUB_STEP_SUMMARY"
  echo "2. Run \`pip-compile requirements.in\` to regenerate \`requirements.txt\`" >> "$GITHUB_STEP_SUMMARY"
  echo "3. Run \`pip-audit -r requirements.txt\` locally to verify the fix" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "❌ **Audit failed — vulnerabilities found!**" >> "$GITHUB_STEP_SUMMARY"
else
  echo "✅ **No vulnerabilities found!**" >> "$GITHUB_STEP_SUMMARY"
fi
