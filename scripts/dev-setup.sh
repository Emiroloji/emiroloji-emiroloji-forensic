#!/bin/bash

# Forensic Face Matching System - Development Setup Script
# This script sets up the development environment for the forensic system

set -e

echo "🔧 Setting up Forensic Face Matching System Development Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    print_status "Checking Docker installation..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    print_success "Docker and Docker Compose are installed"
}

# Check if required tools are installed
check_tools() {
    print_status "Checking required tools..."
    
    local missing_tools=()
    
    if ! command -v java &> /dev/null; then
        missing_tools+=("Java 17+")
    fi
    
    if ! command -v mvn &> /dev/null; then
        missing_tools+=("Maven")
    fi
    
    if ! command -v python3 &> /dev/null; then
        missing_tools+=("Python 3.10+")
    fi
    
    if ! command -v node &> /dev/null; then
        missing_tools+=("Node.js 18+")
    fi
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        print_warning "Missing tools: ${missing_tools[*]}"
        print_warning "Some features may not work without these tools"
    else
        print_success "All required tools are installed"
    fi
}

# Create environment file
setup_environment() {
    print_status "Setting up environment configuration..."
    
    if [ ! -f .env ]; then
        cp .env.example .env
        print_success "Created .env file from template"
        print_warning "Please review and update the .env file with your secure passwords"
    else
        print_status ".env file already exists"
    fi
}

# Create necessary directories
create_directories() {
    print_status "Creating necessary directories..."
    
    mkdir -p logs
    mkdir -p data/postgres
    mkdir -p data/mongodb
    mkdir -p data/redis
    mkdir -p data/minio
    mkdir -p data/ai-models
    mkdir -p nginx/ssl
    mkdir -p monitoring/grafana/dashboards
    mkdir -p monitoring/grafana/datasources
    
    print_success "Created directory structure"
}

# Build Docker images
build_images() {
    print_status "Building Docker images..."
    
    # Build Java services
    print_status "Building Java services..."
    docker-compose build gateway-service auth-service case-service storage-service audit-service
    
    # Build AI service
    print_status "Building AI service..."
    docker-compose build ai-service
    
    # Build frontend
    print_status "Building frontend..."
    docker-compose build frontend
    
    print_success "All Docker images built successfully"
}

# Start infrastructure services
start_infrastructure() {
    print_status "Starting infrastructure services..."
    
    docker-compose up -d postgres mongodb redis rabbitmq minio
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    print_success "Infrastructure services started"
}

# Initialize database
init_database() {
    print_status "Initializing database..."
    
    # Wait for PostgreSQL to be ready
    print_status "Waiting for PostgreSQL to be ready..."
    until docker-compose exec postgres pg_isready -U forensic_user -d forensic_db; do
        sleep 2
    done
    
    # Run database migrations (if any)
    print_status "Running database migrations..."
    # Add migration commands here when available
    
    print_success "Database initialized"
}

# Start application services
start_application() {
    print_status "Starting application services..."
    
    docker-compose up -d gateway-service auth-service case-service storage-service audit-service ai-service
    
    # Wait for services to be ready
    print_status "Waiting for application services to be ready..."
    sleep 60
    
    print_success "Application services started"
}

# Start frontend
start_frontend() {
    print_status "Starting frontend..."
    
    docker-compose up -d frontend
    
    print_success "Frontend started"
}

# Start monitoring
start_monitoring() {
    print_status "Starting monitoring services..."
    
    docker-compose up -d prometheus grafana elasticsearch kibana
    
    print_success "Monitoring services started"
}

# Create admin user
create_admin_user() {
    print_status "Creating admin user..."
    
    # This would typically be done through the API or a management script
    print_warning "Admin user creation not implemented yet"
    print_warning "Please create an admin user through the web interface"
}

# Display status
show_status() {
    print_status "System Status:"
    echo ""
    echo "🌐 Web Interface: http://localhost:3000"
    echo "🔧 API Gateway: http://localhost:8080"
    echo "📊 Grafana: http://localhost:3001 (admin/forensic_grafana_secure_2024_change_this)"
    echo "📈 Prometheus: http://localhost:9090"
    echo "🔍 Kibana: http://localhost:5601"
    echo "🐰 RabbitMQ Management: http://localhost:15672 (forensic_user/forensic_rabbitmq_secure_2024_change_this)"
    echo "📦 MinIO Console: http://localhost:9001 (forensic_admin/forensic_minio_secure_2024_change_this)"
    echo ""
    echo "📋 Services:"
    docker-compose ps
}

# Main execution
main() {
    echo "🚀 Forensic Face Matching System - Development Setup"
    echo "=================================================="
    echo ""
    
    check_docker
    check_tools
    setup_environment
    create_directories
    build_images
    start_infrastructure
    init_database
    start_application
    start_frontend
    start_monitoring
    create_admin_user
    
    echo ""
    print_success "🎉 Development environment setup completed!"
    echo ""
    show_status
    
    echo ""
    print_warning "⚠️  Security Notice:"
    echo "   - Change all default passwords in .env file"
    echo "   - Use HTTPS in production"
    echo "   - Configure proper firewall rules"
    echo "   - Enable audit logging"
    echo ""
    print_status "📚 Next steps:"
    echo "   1. Review and update .env file with secure passwords"
    echo "   2. Access the web interface at http://localhost:3000"
    echo "   3. Create an admin user account"
    echo "   4. Configure your first case"
    echo "   5. Test face comparison functionality"
}

# Run main function
main "$@"
