#!/bin/bash
set -e

# Generate Security Audit Summary from npm audit JSON output
# Usage: ./generate-audit-summary-npm.sh <audit-json-file> <project-label>
# Example: ./generate-audit-summary-npm.sh audit-results.json "TanStack App"

# Guard: skip if GITHUB_STEP_SUMMARY is not set
if [ -z "$GITHUB_STEP_SUMMARY" ]; then
  echo "Warning: GITHUB_STEP_SUMMARY not set, skipping summary generation"
  exit 0
fi

AUDIT_JSON="${1:-}"
PROJECT_LABEL="${2:-npm project}"

if [ ! -f "$AUDIT_JSON" ]; then
  echo "⚠️ Audit JSON file not found: $AUDIT_JSON" >> "$GITHUB_STEP_SUMMARY"
  exit 1
fi

echo "## 🔒 Security Audit — $PROJECT_LABEL" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# Extract vulnerability counts from metadata
info=$(jq -r '.metadata.vulnerabilities.info // 0' "$AUDIT_JSON")
low=$(jq -r '.metadata.vulnerabilities.low // 0' "$AUDIT_JSON")
moderate=$(jq -r '.metadata.vulnerabilities.moderate // 0' "$AUDIT_JSON")
high=$(jq -r '.metadata.vulnerabilities.high // 0' "$AUDIT_JSON")
critical=$(jq -r '.metadata.vulnerabilities.critical // 0' "$AUDIT_JSON")
total=$(jq -r '.metadata.vulnerabilities.total // 0' "$AUDIT_JSON")

# Summary table
echo "### Vulnerability Summary" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"
echo "| Severity | Count |" >> "$GITHUB_STEP_SUMMARY"
echo "|----------|-------|" >> "$GITHUB_STEP_SUMMARY"
[ "$critical" -gt 0 ] && echo "| 🔴 Critical | $critical |" >> "$GITHUB_STEP_SUMMARY"
[ "$high" -gt 0 ] && echo "| 🟠 High | $high |" >> "$GITHUB_STEP_SUMMARY"
[ "$moderate" -gt 0 ] && echo "| 🟡 Moderate | $moderate |" >> "$GITHUB_STEP_SUMMARY"
[ "$low" -gt 0 ] && echo "| 🔵 Low | $low |" >> "$GITHUB_STEP_SUMMARY"
[ "$info" -gt 0 ] && echo "| ℹ️ Info | $info |" >> "$GITHUB_STEP_SUMMARY"
echo "| **Total** | **$total** |" >> "$GITHUB_STEP_SUMMARY"
echo "" >> "$GITHUB_STEP_SUMMARY"

# If there are vulnerabilities, list them
if [ "$total" -gt 0 ]; then
  echo "### Vulnerability Details" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "| Package | Severity | Fix Available | Details |" >> "$GITHUB_STEP_SUMMARY"
  echo "|---------|----------|---------------|---------|" >> "$GITHUB_STEP_SUMMARY"

  # Extract each vulnerability with a direct advisory (not just transitive references)
  jq -r '
    .vulnerabilities | to_entries[] |
    .value |
    select(.via | map(type) | any(. == "object")) |
    {
      name: .name,
      severity: .severity,
      fix: (if .fixAvailable == true then "✅ Yes"
            elif .fixAvailable == false then "❌ No"
            elif .fixAvailable != null then "✅ \(.fixAvailable.name)@\(.fixAvailable.version)"
            else "❓ Unknown" end),
      title: ([.via[] | select(type == "object") | .title] | first // "N/A")
    } |
    "| \(.name) | \(.severity) | \(.fix) | \(.title) |"
  ' "$AUDIT_JSON" >> "$GITHUB_STEP_SUMMARY" 2>/dev/null || true

  echo "" >> "$GITHUB_STEP_SUMMARY"

  # Action items
  echo "### 🛠️ What to do" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "1. Run \`npm audit\` locally to see full details" >> "$GITHUB_STEP_SUMMARY"
  echo "2. Run \`npm audit fix\` to auto-fix where possible" >> "$GITHUB_STEP_SUMMARY"
  echo "3. For breaking fixes: \`npm audit fix --force\` (review changes carefully)" >> "$GITHUB_STEP_SUMMARY"
  echo "" >> "$GITHUB_STEP_SUMMARY"
  echo "❌ **Audit failed — vulnerabilities found!**" >> "$GITHUB_STEP_SUMMARY"
else
  echo "✅ **No vulnerabilities found!**" >> "$GITHUB_STEP_SUMMARY"
fi
