#!/bin/bash

# Forensic Face Match System - Test Execution Script
# ================================================
# This script sets up the environment and runs comprehensive validation tests
# for the forensic face matching system to validate medical-grade precision.

set -e  # Exit on any error

echo "=========================================="
echo "FORENSIC FACE MATCH VALIDATION SUITE"
echo "=========================================="
echo "Medical-Grade Precision and Near-Zero Error Testing"
echo

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "Creating Python virtual environment..."
    python3 -m venv venv
fi

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate

# Upgrade pip
echo "Upgrading pip..."
pip install --upgrade pip

# Install requirements
echo "Installing required packages..."
pip install -r requirements.txt

# Additional test-specific packages
echo "Installing testing packages..."
pip install pytest pytest-html pytest-cov matplotlib seaborn

# Run Python validation suite
echo
echo "Running comprehensive validation suite..."
echo "========================================"

# Set Python path
export PYTHONPATH="${PYTHONPATH}:$(pwd)"

# Run the validation suite
python forensic_validation_suite.py

# Check exit code
if [ $? -eq 0 ]; then
    echo
    echo "✅ VALIDATION SUITE COMPLETED SUCCESSFULLY"
    echo "📊 Check the generated JSON report for detailed results"
else
    echo
    echo "❌ VALIDATION SUITE FAILED"
    echo "🔍 Check the logs for error details"
    exit 1
fi

echo
echo "Test execution completed."
echo "Deactivating virtual environment..."
deactivate
