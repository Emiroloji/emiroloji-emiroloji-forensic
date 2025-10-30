# Docker Compose Setup Guide

## Issues Fixed

### 1. **Missing Environment Variables**
- Created `.env` file with all required configuration
- Set up database credentials, JWT secrets, encryption keys, and service configurations

### 2. **Docker Build Issues**
- Updated `gateway-service/Dockerfile` to install Maven instead of relying on missing Maven Wrapper files (`.mvn` directory and `mvnw` script)
- Fixed ENTRYPOINT to properly reference the built JAR file

### 3. **Docker Compose Configuration**
- Removed obsolete `version` field from `docker-compose.yml` to eliminate warnings

## Environment Variables

The `.env` file contains the following configurations:

\`\`\`
POSTGRES_USER=forensic_user
POSTGRES_PASSWORD=ForensicSecure2024!
JWT_SECRET=your-super-secret-jwt-key-change-in-production-12345678901234567890
ENCRYPTION_KEY=your-encryption-key-change-in-production-1234
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadminsecret
GRAFANA_PASSWORD=admin
MONGO_ROOT_USER=mongoadmin
MONGO_ROOT_PASSWORD=MongoSecure2024!
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
\`\`\`

## ⚠️ Security Notes

The credentials in `.env` are for development purposes only. **Before deploying to production:**

1. Change all passwords to strong, unique values
2. Use a secrets management system (Vault, AWS Secrets Manager, etc.)
3. Never commit `.env` file with production secrets to version control
4. Add `.env` to `.gitignore` if not already there

## How to Run

### 1. Ensure `.env` is in the project root
\`\`\`bash
ls -la .env
\`\`\`

### 2. Build and start services
\`\`\`bash
docker-compose up -d --build
\`\`\`

### 3. View service logs
\`\`\`bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
\`\`\`

### 4. Stop services
\`\`\`bash
docker-compose down
\`\`\`

## Services and Ports

- **Gateway Service**: http://localhost:8080
- **Auth Service**: http://localhost:8081 (internal)
- **Eureka Server**: http://localhost:8761
- **AI Service**: http://localhost:8000 (internal)
- **PostgreSQL**: localhost:5432 (internal)
- **MongoDB**: localhost:27017 (internal)
- **RabbitMQ**: localhost:5672 (internal)
- **Nginx**: http://localhost:80 or :443 (if configured)

## Troubleshooting

### Build fails with "not found" errors
- Ensure `.env` file exists in the root directory
- Run `docker-compose down` and then `docker-compose up -d --build`

### Services won't start
- Check logs: `docker-compose logs`
- Verify ports are not already in use: `lsof -i :8080`
- Ensure Docker has enough resources

### Database connection issues
- Wait 10-15 seconds for PostgreSQL and MongoDB to fully initialize
- Check credentials in `.env` match docker-compose.yml

## Docker Files Modified

1. **docker-compose.yml** - Removed version field
2. **gateway-service/Dockerfile** - Simplified to use system Maven instead of Maven Wrapper
3. **.env** - Created with all required environment variables

## Next Steps

1. Run `docker-compose up -d --build` from the project root
2. Monitor startup with `docker-compose logs -f`
3. Once all services are running, access the application through the Gateway at http://localhost:8080
