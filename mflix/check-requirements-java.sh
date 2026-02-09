#!/bin/bash

# =============================================================================
# Java/Spring Boot Sample App - Requirements Verification Script
# =============================================================================
# This script verifies that all requirements are met to run the Java/Spring Boot
# backend sample application. It checks for required tools, dependencies, and
# environment configuration.
#
# Usage:
#   ./check-requirements-java.sh           # Check all requirements
#   ./check-requirements-java.sh --setup   # Check and auto-setup missing items
# =============================================================================

set -e

# Get script directory (works even if script is sourced)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Configuration - adjusted for artifact repo structure
SERVER_DIR="server"
CLIENT_DIR="client"
JAVA_MIN_VERSION="21"
NODE_MIN_VERSION="18"

# Setup mode flag
SETUP_MODE=false
if [[ "$1" == "--setup" ]]; then
    SETUP_MODE=true
fi

# Counters
CHECKS_PASSED=0
CHECKS_FAILED=0
CHECKS_WARNED=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# =============================================================================
# Helper Functions
# =============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}======================================${NC}"
}

print_subheader() {
    echo ""
    echo -e "${BLUE}--- $1 ---${NC}"
}

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
    echo -e "  ${BLUE}→${NC} $1"
}

version_gte() {
    # Returns 0 (true) if $1 >= $2
    [ "$1" -ge "$2" ] 2>/dev/null
}

command_exists() {
    command -v "$1" &>/dev/null
}

# =============================================================================
# Common Requirements
# =============================================================================

check_common_requirements() {
    print_subheader "Common Requirements"

    # Check Git
    if command_exists git; then
        GIT_VERSION=$(git --version | grep -oE '[0-9]+\.[0-9]+' | head -1)
        check_pass "Git installed (v$GIT_VERSION)"
    else
        check_fail "Git not installed"
        check_info "Install Git from https://git-scm.com/"
    fi

    # Check curl
    if command_exists curl; then
        check_pass "curl installed"
    else
        check_warn "curl not installed (optional, but useful for testing)"
    fi
}

# =============================================================================
# Java/Spring Boot Requirements
# =============================================================================

check_java_requirements() {
    print_subheader "Java/Spring Boot Backend"

    local server_dir="$SCRIPT_DIR/$SERVER_DIR"

    # Check Java
    if command_exists java; then
        # Get Java version - handle different version formats
        JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -n1)
        JAVA_VERSION=$(echo "$JAVA_VERSION_OUTPUT" | sed -E 's/.*version "([0-9]+).*/\1/')

        if [ -n "$JAVA_VERSION" ] && version_gte "$JAVA_VERSION" "$JAVA_MIN_VERSION"; then
            check_pass "Java installed: version $JAVA_VERSION (>= $JAVA_MIN_VERSION required)"
        else
            check_fail "Java version $JAVA_VERSION is too old (>= $JAVA_MIN_VERSION required)"
            check_info "Install Java $JAVA_MIN_VERSION+ from:"
            check_info "  - Eclipse Temurin: https://adoptium.net/"
            check_info "  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/"
            check_info "  - Or use SDKMAN: https://sdkman.io/"
        fi
    else
        check_fail "Java not installed"
        check_info "Install Java $JAVA_MIN_VERSION+ from:"
        check_info "  - Eclipse Temurin: https://adoptium.net/"
        check_info "  - Oracle JDK: https://www.oracle.com/java/technologies/downloads/"
        check_info "  - Or use SDKMAN: https://sdkman.io/"
        return
    fi

    # Check JAVA_HOME
    if [ -n "$JAVA_HOME" ]; then
        if [ -d "$JAVA_HOME" ]; then
            check_pass "JAVA_HOME is set: $JAVA_HOME"
        else
            check_warn "JAVA_HOME is set but directory doesn't exist: $JAVA_HOME"
        fi
    else
        check_warn "JAVA_HOME is not set (may cause issues with some tools)"
        check_info "Set JAVA_HOME to your Java installation directory"
    fi


    # Check Maven wrapper
    if [ -f "$server_dir/mvnw" ]; then
        check_pass "Maven wrapper (mvnw) found"

        # Check if mvnw is executable
        if [ -x "$server_dir/mvnw" ]; then
            check_pass "Maven wrapper is executable"
        else
            check_warn "Maven wrapper is not executable"
            if [ "$SETUP_MODE" = true ]; then
                chmod +x "$server_dir/mvnw"
                check_pass "Made Maven wrapper executable"
            else
                check_info "Run: chmod +x $server_dir/mvnw"
            fi
        fi

        # Try to get Maven version
        MAVEN_VERSION=$(cd "$server_dir" && ./mvnw --version 2>/dev/null | grep "Apache Maven" | awk '{print $3}')
        if [ -n "$MAVEN_VERSION" ]; then
            check_pass "Maven version: $MAVEN_VERSION"
        fi
    else
        check_fail "Maven wrapper (mvnw) not found in $server_dir"
        check_info "The Maven wrapper should be included in the repository"
    fi

    # Check if Maven dependencies are downloaded
    if [ -d "$server_dir/target" ]; then
        check_pass "Maven target directory exists (dependencies likely downloaded)"
    else
        check_warn "Maven target directory not found"
        if [ "$SETUP_MODE" = true ]; then
            check_info "Downloading Maven dependencies..."
            if (cd "$server_dir" && ./mvnw dependency:resolve -q); then
                check_pass "Maven dependencies downloaded successfully"
            else
                check_fail "Failed to download Maven dependencies"
            fi
        else
            check_info "Run: cd $server_dir && ./mvnw dependency:resolve"
        fi
    fi

    # Check if project compiles
    if [ -d "$server_dir/target/classes" ]; then
        check_pass "Project appears to be compiled"
    else
        check_warn "Project not compiled yet"
        if [ "$SETUP_MODE" = true ]; then
            check_info "Compiling project..."
            if (cd "$server_dir" && ./mvnw compile -q); then
                check_pass "Project compiled successfully"
            else
                check_fail "Failed to compile project"
            fi
        else
            check_info "Run: cd $server_dir && ./mvnw compile"
        fi
    fi
}

# =============================================================================
# Environment Configuration
# =============================================================================

check_env_configuration() {
    print_subheader "Environment Configuration"

    local server_dir="$SCRIPT_DIR/$SERVER_DIR"
    local env_file="$server_dir/.env"
    local example_file="$server_dir/.env.example"

    if [ -f "$env_file" ]; then
        check_pass ".env file exists"

        # Check MongoDB URI
        if grep -q "^MONGODB_URI=" "$env_file" 2>/dev/null; then
            if grep -qE "^MONGODB_URI=.*<.*>" "$env_file" 2>/dev/null; then
                check_warn "MONGODB_URI appears to be a placeholder - update with your connection string"
            elif grep -qE "^MONGODB_URI=.+" "$env_file" 2>/dev/null; then
                check_pass "MONGODB_URI is configured"
            else
                check_warn "MONGODB_URI is empty"
            fi
        else
            check_fail "MONGODB_URI not found in .env"
        fi

        # Check Voyage AI key (optional)
        if grep -q "^VOYAGE_API_KEY=" "$env_file" 2>/dev/null; then
            if grep -qE "^VOYAGE_API_KEY=your" "$env_file" 2>/dev/null || \
               grep -qE "^VOYAGE_API_KEY=$" "$env_file" 2>/dev/null; then
                check_info "VOYAGE_API_KEY not configured (optional - needed for vector search)"
            else
                check_pass "VOYAGE_API_KEY is configured"
            fi
        else
            check_info "VOYAGE_API_KEY not found (optional - needed for vector search)"
        fi

        # Check CORS_ORIGINS
        if grep -q "^CORS_ORIGINS=" "$env_file" 2>/dev/null; then
            check_pass "CORS_ORIGINS is configured"
        else
            check_info "CORS_ORIGINS not found (will use default: http://localhost:3000)"
        fi

        # Check PORT
        if grep -q "^PORT=" "$env_file" 2>/dev/null; then
            check_pass "PORT is configured"
        else
            check_info "PORT not found (will use default)"
        fi
    else
        check_warn ".env file not found"
        if [ -f "$example_file" ]; then
            check_info ".env.example exists - use it as a template"
            if [ "$SETUP_MODE" = true ]; then
                cp "$example_file" "$env_file"
                check_pass "Created .env from .env.example"
                check_warn "Please edit $env_file with your actual values"
            else
                check_info "Run: cp $example_file $env_file"
                check_info "Then edit .env with your actual values"
            fi
        else
            check_fail "No .env.example found to use as template"
        fi
    fi
}


# =============================================================================
# Frontend Requirements
# =============================================================================

check_frontend_requirements() {
    print_header "Frontend Requirements (Next.js)"

    local client_dir="$SCRIPT_DIR/$CLIENT_DIR"

    # Check Node.js (required for frontend)
    print_subheader "Node.js"
    if command_exists node; then
        NODE_VERSION=$(node --version 2>/dev/null | sed 's/v//')
        NODE_MAJOR=$(echo "$NODE_VERSION" | cut -d. -f1)

        if [ "$NODE_MAJOR" -ge "$NODE_MIN_VERSION" ] 2>/dev/null; then
            check_pass "Node.js installed: v$NODE_VERSION (required: >= $NODE_MIN_VERSION)"
        else
            check_fail "Node.js version $NODE_VERSION is too old (required: >= $NODE_MIN_VERSION)"
            check_info "Install Node.js $NODE_MIN_VERSION+ from: https://nodejs.org/"
        fi
    else
        check_fail "Node.js not installed"
        check_info "Install Node.js $NODE_MIN_VERSION+ from: https://nodejs.org/"
        return
    fi

    # Check npm
    print_subheader "npm"
    if command_exists npm; then
        NPM_VERSION=$(npm --version 2>/dev/null)
        check_pass "npm installed: v$NPM_VERSION"
    else
        check_fail "npm not installed"
        check_info "npm should be installed with Node.js"
        return
    fi

    # Check client dependencies
    print_subheader "Frontend Dependencies"
    if [ -d "$client_dir/node_modules" ]; then
        check_pass "Frontend dependencies installed"

        # Check for Next.js
        if [ -d "$client_dir/node_modules/next" ]; then
            check_pass "Next.js is installed"
        else
            check_warn "Next.js not found in dependencies"
        fi

        # Check for React
        if [ -d "$client_dir/node_modules/react" ]; then
            check_pass "React is installed"
        else
            check_warn "React not found in dependencies"
        fi
    else
        check_warn "Frontend dependencies not installed"
        if [ "$SETUP_MODE" = true ]; then
            check_info "Installing frontend dependencies..."
            if (cd "$client_dir" && npm install); then
                check_pass "Frontend dependencies installed successfully"
            else
                check_fail "Failed to install frontend dependencies"
            fi
        else
            check_info "Run: cd $client_dir && npm install"
        fi
    fi
}

# =============================================================================
# Summary
# =============================================================================

print_summary() {
    echo ""
    echo "============================================================================="
    echo "                              SUMMARY"
    echo "============================================================================="
    echo ""

    if [ "$CHECKS_FAILED" -eq 0 ]; then
        echo -e "${GREEN}✓ All checks passed!${NC}"
    else
        echo -e "${RED}✗ Some checks failed${NC}"
    fi

    echo ""
    echo -e "  ${GREEN}Passed:${NC}   $CHECKS_PASSED"
    echo -e "  ${RED}Failed:${NC}   $CHECKS_FAILED"
    echo -e "  ${YELLOW}Warnings:${NC} $CHECKS_WARNED"
    echo ""

    if [ "$CHECKS_FAILED" -gt 0 ]; then
        echo "Review the failed checks above and follow the instructions to resolve them."
        echo "Run with --setup flag to automatically fix some issues: ./check-requirements-java.sh --setup"
        echo ""
    fi
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    echo "============================================================================="
    echo "        Java/Spring Boot Backend - Requirements Verification"
    echo "============================================================================="
    echo ""

    check_common_requirements
    check_java_requirements
    check_env_configuration
    check_frontend_requirements
    print_summary

    # Exit with error code if any checks failed
    if [ "$CHECKS_FAILED" -gt 0 ]; then
        exit 1
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --setup)
            SETUP_MODE=true
            shift
            ;;
        --help|-h)
            echo "Usage: ./check-requirements-java.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --setup    Automatically set up missing requirements where possible"
            echo "  --help     Show this help message"
            echo ""
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

main