#!/bin/bash

# =============================================================================
# Requirements Verification Script for mflix Sample Application
# Python/FastAPI Backend
# =============================================================================
# This script checks that all necessary requirements are installed to run
# the mflix sample application with the Python/FastAPI backend.
#
# Usage: ./check-requirements.sh [options]
#   --setup    Attempt to set up missing requirements
#   --help     Show this help message
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
CHECKS_PASSED=0
CHECKS_FAILED=0
CHECKS_WARNED=0

# Options
SETUP_MODE=false

# Configuration
PYTHON_MIN_VERSION="3.11"
SERVER_DIR="server"
CLIENT_DIR="client"

# =============================================================================
# Helper Functions
# =============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_subheader() {
    echo ""
    echo -e "${YELLOW}▸ $1${NC}"
}

check_pass() {
    echo -e "  ${GREEN}✓${NC} $1"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
}

check_fail() {
    echo -e "  ${RED}✗${NC} $1"
    CHECKS_FAILED=$((CHECKS_FAILED + 1))
}

check_warn() {
    echo -e "  ${YELLOW}⚠${NC} $1"
    CHECKS_WARNED=$((CHECKS_WARNED + 1))
}

check_info() {
    echo -e "  ${BLUE}ℹ${NC} $1"
}

version_gte() {
    [ "$(printf '%s\n' "$2" "$1" | sort -V | head -n1)" = "$2" ]
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# =============================================================================
# Parse Arguments
# =============================================================================

show_help() {
    echo "Usage: ./check-requirements.sh [options]"
    echo ""
    echo "Options:"
    echo "  --setup    Attempt to set up missing requirements"
    echo "  --help     Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./check-requirements.sh           # Check all requirements"
    echo "  ./check-requirements.sh --setup   # Check and set up missing items"
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --setup)
            SETUP_MODE=true
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# =============================================================================
# Get Script Directory
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

print_header "mflix Sample Application - Python/FastAPI Requirements Check"
echo ""
echo "Setup mode: $SETUP_MODE"
echo "Working directory: $SCRIPT_DIR"

# =============================================================================
# Common Requirements
# =============================================================================

check_common_requirements() {
    print_subheader "Common Requirements"

    # Check Git
    if command_exists git; then
        local git_version
        git_version=$(git --version | awk '{print $3}')
        check_pass "Git installed (version $git_version)"
    else
        check_fail "Git not installed"
        check_info "Install Git: https://git-scm.com/downloads"
    fi

    # Check curl
    if command_exists curl; then
        check_pass "curl installed"
    else
        check_fail "curl not installed"
        check_info "Install curl using your package manager"
    fi
}

# =============================================================================
# Python Backend Requirements
# =============================================================================

check_python_requirements() {
    print_subheader "Python/FastAPI Backend Requirements"

    # Check Python version
    if command_exists python3; then
        PYTHON_VERSION=$(python3 --version 2>&1 | grep -oE '[0-9]+\.[0-9]+' | head -1)
        if version_gte "$PYTHON_VERSION" "$PYTHON_MIN_VERSION"; then
            check_pass "Python $PYTHON_VERSION installed (>= $PYTHON_MIN_VERSION required)"
        else
            check_fail "Python $PYTHON_VERSION installed but >= $PYTHON_MIN_VERSION required"
            check_info "Install Python $PYTHON_MIN_VERSION+ from https://www.python.org/downloads/"
        fi
    else
        check_fail "Python 3 not installed"
        check_info "Install Python $PYTHON_MIN_VERSION+ from https://www.python.org/downloads/"
    fi

    # Check pip
    if command_exists pip3 || python3 -m pip --version &>/dev/null; then
        check_pass "pip installed"
    else
        check_fail "pip not installed"
        check_info "Install pip: python3 -m ensurepip --upgrade"
    fi

    # Check virtual environment
    local venv_dir="$SCRIPT_DIR/$SERVER_DIR/.venv"
    if [[ -d "$venv_dir" ]]; then
        check_pass "Python virtual environment exists at $SERVER_DIR/.venv"

        # Check if venv is activated or can be used
        if [[ -f "$venv_dir/bin/activate" ]]; then
            check_pass "Virtual environment activation script exists"
        else
            check_warn "Virtual environment activation script missing"
        fi
    else
        check_warn "Python virtual environment not found at $SERVER_DIR/.venv"
        if [[ "$SETUP_MODE" == true ]]; then
            check_info "Creating virtual environment..."
            if python3 -m venv "$venv_dir"; then
                check_pass "Virtual environment created"
            else
                check_fail "Failed to create virtual environment"
            fi
        else
            check_info "Create with: cd $SERVER_DIR && python3 -m venv .venv"
        fi
    fi

    # Check Python dependencies
    local requirements_file="$SCRIPT_DIR/$SERVER_DIR/requirements.txt"
    if [[ -f "$requirements_file" ]]; then
        check_pass "requirements.txt found"

        # Check if key dependencies are installed
        if [[ -d "$venv_dir" ]]; then
            local pip_cmd="$venv_dir/bin/pip"
            if [[ -f "$pip_cmd" ]]; then
                # Check FastAPI
                if "$pip_cmd" show fastapi &>/dev/null; then
                    check_pass "FastAPI installed in virtual environment"
                else
                    check_warn "FastAPI not installed in virtual environment"
                    if [[ "$SETUP_MODE" == true ]]; then
                        check_info "Installing dependencies..."
                        if "$pip_cmd" install -r "$requirements_file" &>/dev/null; then
                            check_pass "Dependencies installed"
                        else
                            check_fail "Failed to install dependencies"
                        fi
                    else
                        check_info "Install with: source $SERVER_DIR/.venv/bin/activate && pip install -r $SERVER_DIR/requirements.txt"
                    fi
                fi

                # Check PyMongo
                if "$pip_cmd" show pymongo &>/dev/null; then
                    check_pass "PyMongo installed in virtual environment"
                else
                    check_warn "PyMongo not installed in virtual environment"
                fi
            fi
        fi
    else
        check_fail "requirements.txt not found at $SERVER_DIR/requirements.txt"
    fi
}

# =============================================================================
# Environment Configuration
# =============================================================================

check_env_configuration() {
    print_subheader "Environment Configuration"

    local env_file="$SCRIPT_DIR/$SERVER_DIR/.env"
    local env_example="$SCRIPT_DIR/$SERVER_DIR/.env.example"

    # Check .env file exists
    if [[ -f "$env_file" ]]; then
        check_pass ".env file exists at $SERVER_DIR/.env"
    else
        check_warn ".env file not found at $SERVER_DIR/.env"
        if [[ "$SETUP_MODE" == true ]] && [[ -f "$env_example" ]]; then
            check_info "Copying .env.example to .env..."
            if cp "$env_example" "$env_file"; then
                check_pass ".env file created from .env.example"
                check_info "Please update the values in $SERVER_DIR/.env"
            else
                check_fail "Failed to create .env file"
            fi
        else
            check_info "Copy the example: cp $SERVER_DIR/.env.example $SERVER_DIR/.env"
        fi
        return
    fi

    # Check required environment variables
    if grep -q "^MONGODB_URI=" "$env_file" 2>/dev/null; then
        local mongo_uri=$(grep "^MONGODB_URI=" "$env_file" | cut -d'=' -f2-)
        if [[ -n "$mongo_uri" && "$mongo_uri" != "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/" ]]; then
            check_pass "MONGODB_URI is configured"
        else
            check_fail "MONGODB_URI is not configured (still has placeholder value)"
            check_info "Update MONGODB_URI in $SERVER_DIR/.env with your MongoDB connection string"
        fi
    else
        check_fail "MONGODB_URI not found in .env"
        check_info "Add MONGODB_URI to $SERVER_DIR/.env"
    fi

    # Check optional environment variables
    if grep -q "^VOYAGE_API_KEY=" "$env_file" 2>/dev/null; then
        local voyage_key=$(grep "^VOYAGE_API_KEY=" "$env_file" | cut -d'=' -f2-)
        if [[ -n "$voyage_key" && "$voyage_key" != "<your-voyage-api-key>" ]]; then
            check_pass "VOYAGE_API_KEY is configured"
        else
            check_info "VOYAGE_API_KEY not configured (optional - needed for vector search)"
        fi
    else
        check_info "VOYAGE_API_KEY not set (optional - needed for vector search)"
    fi

    if grep -q "^CORS_ORIGINS=" "$env_file" 2>/dev/null; then
        check_pass "CORS_ORIGINS is configured"
    else
        check_info "CORS_ORIGINS not set (will use default: http://localhost:3000)"
    fi

    if grep -q "^PORT=" "$env_file" 2>/dev/null; then
        check_pass "PORT is configured"
    else
        check_info "PORT not set (will use default: 8000)"
    fi
}



# =============================================================================
# Frontend Requirements (Next.js)
# =============================================================================

check_frontend_requirements() {
    print_subheader "Frontend Requirements (Next.js)"

    # Check Node.js version
    if command_exists node; then
        NODE_VERSION=$(node --version 2>&1 | grep -oE '[0-9]+' | head -1)
        if version_gte "$NODE_VERSION" "$NODE_MIN_VERSION"; then
            check_pass "Node.js v$NODE_VERSION installed (>= v$NODE_MIN_VERSION required)"
        else
            check_fail "Node.js v$NODE_VERSION installed but >= v$NODE_MIN_VERSION required"
        fi
    else
        check_fail "Node.js not installed"
        check_info "Install Node.js $NODE_MIN_VERSION+ from https://nodejs.org/"
    fi

    # Check npm
    if command_exists npm; then
        check_pass "npm installed"
    else
        check_fail "npm not installed"
    fi

    # Check client dependencies
    local client_dir="$SCRIPT_DIR/$CLIENT_DIR"
    if [[ -d "$client_dir" ]]; then
        if [[ -d "$client_dir/node_modules" ]]; then
            check_pass "Client dependencies installed"
        else
            check_warn "Client dependencies not installed"
            if [[ "$SETUP_MODE" == true ]]; then
                check_info "Installing client dependencies..."
                if (cd "$client_dir" && npm install &>/dev/null); then
                    check_pass "Client dependencies installed"
                else
                    check_fail "Failed to install client dependencies"
                fi
            else
                check_info "Install with: cd $CLIENT_DIR && npm install"
            fi
        fi
    fi
}

# =============================================================================
# Summary
# =============================================================================

print_summary() {
    echo ""
    print_header "Summary"
    echo -e "${GREEN}Passed:${NC}  $CHECKS_PASSED"
    echo -e "${RED}Failed:${NC}  $CHECKS_FAILED"
    echo -e "${YELLOW}Warnings:${NC} $CHECKS_WARNED"
    echo ""

    if [[ $CHECKS_FAILED -gt 0 ]]; then
        echo -e "${RED}Some checks failed. Please address the issues above.${NC}"
        exit 1
    elif [[ $CHECKS_WARNED -gt 0 ]]; then
        echo -e "${YELLOW}All critical checks passed, but there are warnings to review.${NC}"
        exit 0
    else
        echo -e "${GREEN}All checks passed! You're ready to run the application.${NC}"
        exit 0
    fi
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    print_header "Python/FastAPI Sample App - Requirements Check"

    check_common_requirements
    check_python_requirements
    check_env_configuration
    check_frontend_requirements

    print_summary
}

main
