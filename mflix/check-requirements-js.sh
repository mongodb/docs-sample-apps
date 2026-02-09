#!/bin/bash
#
# Requirements Verification Script for mflix Sample Application
# JavaScript/Express Backend (Node.js + Express + MongoDB)
#
# This script checks if you have all the necessary requirements to run the
# mflix sample application with the JavaScript/Express backend.
#
# Usage:
#   ./check-requirements-js.sh           # Check all requirements
#   ./check-requirements-js.sh --setup   # Check and auto-setup missing items
#   ./check-requirements-js.sh --help    # Show help message
#

set -e

# =============================================================================
# Configuration
# =============================================================================

# Server directory (in artifact repo, server/js-express becomes just server)
SERVER_DIR="server"

# =============================================================================
# Colors for output
# =============================================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =============================================================================
# Counters
# =============================================================================

CHECKS_PASSED=0
CHECKS_FAILED=0
CHECKS_WARNED=0

# =============================================================================
# Helper Functions
# =============================================================================

check_pass() {
    echo -e "${GREEN}✓${NC} $1"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
}

check_fail() {
    echo -e "${RED}✗${NC} $1"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
}

check_warn() {
    echo -e "${YELLOW}!${NC} $1"
    CHECKS_WARNED=$((CHECKS_WARNED + 1))
}

check_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_section() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# =============================================================================
# Check Common Requirements
# =============================================================================

check_common_requirements() {
    print_section "Common Requirements"

    # Check Git
    if command -v git &> /dev/null; then
        local git_version=$(git --version | cut -d' ' -f3)
        check_pass "Git installed (version $git_version)"
    else
        check_fail "Git not installed"
        check_info "Install Git: https://git-scm.com/downloads"
    fi

    # Check curl (useful for API testing)
    if command -v curl &> /dev/null; then
        check_pass "curl installed"
    else
        check_warn "curl not installed (optional, useful for API testing)"
    fi
}

# =============================================================================
# Check Node.js/Express Requirements
# =============================================================================

check_node_requirements() {
    print_section "Node.js/Express Backend Requirements"

    # Check Node.js
    if command -v node &> /dev/null; then
        local node_version=$(node --version | sed 's/v//')
        local node_major=$(echo "$node_version" | cut -d. -f1)
        if [ "$node_major" -ge 18 ]; then
            check_pass "Node.js installed (version $node_version)"
        else
            check_fail "Node.js version $node_version is below minimum required (18+)"
            check_info "Install Node.js 18+: https://nodejs.org/"
        fi
    else
        check_fail "Node.js not installed"
        check_info "Install Node.js 18+: https://nodejs.org/"
        return
    fi

    # Check npm
    if command -v npm &> /dev/null; then
        local npm_version=$(npm --version)
        check_pass "npm installed (version $npm_version)"
    else
        check_fail "npm not installed"
        check_info "npm should come with Node.js installation"
        return
    fi

    # Check server directory
    if [ ! -d "$SERVER_DIR" ]; then
        check_fail "Server directory not found: $SERVER_DIR"
        return
    fi

    # Check package.json
    if [ -f "$SERVER_DIR/package.json" ]; then
        check_pass "package.json found"
    else
        check_fail "package.json not found in $SERVER_DIR"
        return
    fi

    # Check node_modules
    if [ -d "$SERVER_DIR/node_modules" ]; then
        check_pass "node_modules directory exists"

        # Check key dependencies
        if [ -d "$SERVER_DIR/node_modules/express" ]; then
            check_pass "Express.js dependency installed"
        else
            check_fail "Express.js dependency not installed"
        fi

        if [ -d "$SERVER_DIR/node_modules/mongodb" ]; then
            check_pass "MongoDB driver dependency installed"
        else
            check_fail "MongoDB driver dependency not installed"
        fi

        # Check TypeScript build
        if [ -d "$SERVER_DIR/dist" ]; then
            check_pass "TypeScript build output exists (dist directory)"
        else
            check_warn "TypeScript build output not found (dist directory)"
            check_info "Run 'npm run build' in $SERVER_DIR to build"
        fi
    else
        check_fail "node_modules directory not found"
        if [ "$SETUP_MODE" = true ]; then
            check_info "Attempting to install dependencies..."
            if (cd "$SERVER_DIR" && npm install); then
                check_pass "Dependencies installed successfully"
            else
                check_fail "Failed to install dependencies"
                check_info "Run 'npm install' manually in $SERVER_DIR"
            fi
        else
            check_info "Run 'npm install' in $SERVER_DIR or use --setup flag"
        fi
    fi
}

# =============================================================================
# Check Environment Configuration
# =============================================================================

check_env_configuration() {
    print_section "Environment Configuration"

    local env_file="$SERVER_DIR/.env"
    local env_example="$SERVER_DIR/.env.example"

    # Check .env file
    if [ -f "$env_file" ]; then
        check_pass ".env file exists"

        # Check MONGODB_URI
        if grep -q "^MONGODB_URI=" "$env_file" 2>/dev/null; then
            local mongo_uri=$(grep "^MONGODB_URI=" "$env_file" | cut -d'=' -f2-)
            if [ -n "$mongo_uri" ] && [ "$mongo_uri" != "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/" ]; then
                check_pass "MONGODB_URI is configured"
            else
                check_fail "MONGODB_URI is not configured (still has placeholder value)"
                check_info "Update MONGODB_URI in $env_file with your MongoDB connection string"
            fi
        else
            check_fail "MONGODB_URI not found in .env"
            check_info "Add MONGODB_URI=<your-connection-string> to $env_file"
        fi

        # Check VOYAGE_API_KEY (optional)
        if grep -q "^VOYAGE_API_KEY=" "$env_file" 2>/dev/null; then
            local voyage_key=$(grep "^VOYAGE_API_KEY=" "$env_file" | cut -d'=' -f2-)
            if [ -n "$voyage_key" ] && [ "$voyage_key" != "<your-voyage-api-key>" ]; then
                check_pass "VOYAGE_API_KEY is configured"
            else
                check_warn "VOYAGE_API_KEY has placeholder value (optional for vector search)"
            fi
        else
            check_info "VOYAGE_API_KEY not set (optional, needed for vector search features)"
        fi

        # Check CORS_ORIGINS (optional)
        if grep -q "^CORS_ORIGINS=" "$env_file" 2>/dev/null; then
            check_pass "CORS_ORIGINS is configured"
        else
            check_info "CORS_ORIGINS not set (will use default: http://localhost:3000)"
        fi

        # Check PORT (optional)
        if grep -q "^PORT=" "$env_file" 2>/dev/null; then
            local port=$(grep "^PORT=" "$env_file" | cut -d'=' -f2-)
            check_pass "PORT is configured ($port)"
        else
            check_info "PORT not set (will use default: 3001)"
        fi

        # Check LOG_LEVEL (optional)
        if grep -q "^LOG_LEVEL=" "$env_file" 2>/dev/null; then
            check_pass "LOG_LEVEL is configured"
        else
            check_info "LOG_LEVEL not set (will use default)"
        fi
    else
        check_fail ".env file not found"
        if [ -f "$env_example" ]; then
            if [ "$SETUP_MODE" = true ]; then
                check_info "Attempting to create .env from .env.example..."
                if cp "$env_example" "$env_file"; then
                    check_pass ".env file created from .env.example"
                    check_warn "Please update the placeholder values in $env_file"
                else
                    check_fail "Failed to create .env file"
                fi
            else
                check_info "Copy .env.example to .env: cp $env_example $env_file"
                check_info "Or use --setup flag to create automatically"
            fi
        else
            check_info "Create a .env file with required configuration"
            check_info "Required: MONGODB_URI"
            check_info "Optional: VOYAGE_API_KEY, CORS_ORIGINS, PORT, LOG_LEVEL"
        fi
    fi
}

# =============================================================================
# Check Frontend Requirements
# =============================================================================

check_frontend_requirements() {
    print_section "Frontend Requirements (Next.js)"

    local client_dir="client"

    # Check client directory
    if [ ! -d "$client_dir" ]; then
        check_warn "Client directory not found: $client_dir"
        check_info "Frontend may be in a separate repository"
        return
    fi

    # Check package.json
    if [ -f "$client_dir/package.json" ]; then
        check_pass "Frontend package.json found"
    else
        check_fail "Frontend package.json not found"
        return
    fi

    # Check node_modules
    if [ -d "$client_dir/node_modules" ]; then
        check_pass "Frontend node_modules exists"

        # Check Next.js
        if [ -d "$client_dir/node_modules/next" ]; then
            check_pass "Next.js dependency installed"
        else
            check_fail "Next.js dependency not installed"
        fi

        # Check React
        if [ -d "$client_dir/node_modules/react" ]; then
            check_pass "React dependency installed"
        else
            check_fail "React dependency not installed"
        fi
    else
        check_fail "Frontend node_modules not found"
        if [ "$SETUP_MODE" = true ]; then
            check_info "Attempting to install frontend dependencies..."
            if (cd "$client_dir" && npm install); then
                check_pass "Frontend dependencies installed successfully"
            else
                check_fail "Failed to install frontend dependencies"
                check_info "Run 'npm install' manually in $client_dir"
            fi
        else
            check_info "Run 'npm install' in $client_dir or use --setup flag"
        fi
    fi
}

# =============================================================================
# Print Summary
# =============================================================================

print_summary() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  Summary${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "  ${GREEN}Passed:${NC}  $CHECKS_PASSED"
    echo -e "  ${RED}Failed:${NC}  $CHECKS_FAILED"
    echo -e "  ${YELLOW}Warnings:${NC} $CHECKS_WARNED"
    echo ""

    if [ $CHECKS_FAILED -eq 0 ]; then
        echo -e "${GREEN}All required checks passed!${NC}"
        if [ $CHECKS_WARNED -gt 0 ]; then
            echo -e "${YELLOW}There are some warnings to review.${NC}"
        fi
    else
        echo -e "${RED}Some checks failed. Please address the issues above.${NC}"
        if [ "$SETUP_MODE" != true ]; then
            echo -e "${BLUE}Tip: Run with --setup flag to auto-fix some issues${NC}"
        fi
    fi
    echo ""
}

# =============================================================================
# Main Execution
# =============================================================================

SETUP_MODE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --setup)
            SETUP_MODE=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --setup    Attempt to automatically set up missing requirements"
            echo "  --help     Show this help message"
            echo ""
            echo "This script checks if you have all the necessary requirements"
            echo "to run the mflix sample application with the JavaScript/Express backend."
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  mflix Sample Application - Requirements Check               ║${NC}"
echo -e "${BLUE}║  JavaScript/Express Backend                                  ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"

if [ "$SETUP_MODE" = true ]; then
    echo -e "${YELLOW}Running in setup mode - will attempt to fix issues${NC}"
fi

# Run all checks
check_common_requirements
check_node_requirements
check_env_configuration
check_frontend_requirements

# Print summary
print_summary

# Exit with appropriate code
if [ $CHECKS_FAILED -gt 0 ]; then
    exit 1
fi
exit 0
