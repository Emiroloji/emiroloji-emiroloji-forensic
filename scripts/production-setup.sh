#!/bin/bash

echo "Setting up production environment..."

# Create .env file from .env.example if it doesn't exist
if [ ! -f .env ]; then
  echo "Creating .env file from .env.example"
  cp .env.example .env
  echo "Please update .env file with production values!"
  exit 1
fi

# Generate secure random keys
echo "Generating secure random keys..."
JWT_SECRET=$(openssl rand -base64 64)
ENCRYPTION_KEY=$(openssl rand -base64 32)

# Update .env file with generated keys
sed -i "s/YourSuperSecretJwtKeyThatIsAtLeast256BitsLongAndShouldBeRandomlyGeneratedInProduction/$JWT_SECRET/" .env
sed -i "s/Your32ByteEncryptionKeyForAES256/$ENCRYPTION_KEY/" .env

# Create SSL certificates directory
mkdir -p nginx/ssl

# Generate self-signed certificate for development
echo "Generating self-signed SSL certificate..."
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/nginx-selfsigned.key \
  -out nginx/ssl/nginx-selfsigned.crt \
  -subj "/C=TR/ST=Istanbul/L=Istanbul/O=Forensic/OU=IT/CN=localhost"

# Build and start Docker containers
echo "Building and starting Docker containers..."
docker-compose up --build -d

echo "Production environment setup complete."
echo "Services available at:"
echo "- Frontend: http://localhost:3000"
echo "- API Gateway: http://localhost:8080"
echo "- Eureka Server: http://localhost:8761"
echo "- MinIO Console: http://localhost:9001"
echo "- RabbitMQ Management: http://localhost:15672"
echo "- Prometheus: http://localhost:9090"
echo "- Grafana: http://localhost:3001"
echo "- Kibana: http://localhost:5601"
echo "- Elasticsearch: http://localhost:9200"
