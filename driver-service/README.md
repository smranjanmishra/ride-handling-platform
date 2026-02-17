# Driver Service

A microservice for managing driver operations in a ride-booking system. This service handles driver registration, authentication, location tracking, availability management, and ride acceptance workflows.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Docker](#docker)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Monitoring](#monitoring)
- [Security](#security)

## ✨ Features

- **Driver Management**
  - Driver registration with validation
  - Driver authentication with JWT tokens
  - Driver profile retrieval

- **Location Services**
  - Real-time location updates
  - Location-based driver search
  - Distance calculation for nearby drivers

- **Availability Management**
  - Driver availability status updates
  - Search for available drivers within a radius
  - Availability status tracking

- **Ride Management**
  - Accept ride requests
  - Start rides
  - Complete rides with fare calculation
  - Driver earnings tracking

- **Security**
  - JWT-based authentication
  - Password encryption
  - Rate limiting
  - Request validation

- **Observability**
  - Health checks via Spring Actuator
  - Prometheus metrics
  - Structured logging with correlation IDs
  - Request/response logging

## 🛠 Tech Stack

- **Framework**: Spring Boot 6.2.11
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security 6.4.10 with JWT
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Logging**: Log4j2
- **Monitoring**: Micrometer, Prometheus
- **API Documentation**: RESTful APIs

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+** (or use the included Maven wrapper)
- **PostgreSQL 12+**
- **Docker** (optional, for containerized deployment)
- **Kubernetes** (optional, for cluster deployment)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd driver-service
```

### 2. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE driver_db;
```

The application will automatically run Flyway migrations on startup to create the required tables.

### 3. Configure Application

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/driver_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build the Project

```bash
./mvnw clean install
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

The service will start on `http://localhost:8082` by default.

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8082
spring.application.name=driver-service

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/driver_db
spring.datasource.username=postgres
spring.datasource.password=admin

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Service URLs
services.rider-service.url=http://localhost:8081
services.notification-service.url=http://localhost:8083

# JWT Configuration
jwt.secret=YourSecretKeyHere
jwt.expiration=86400000  # 24 hours in milliseconds

# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

### Environment Variables

For production, use environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/driver_db
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your-secret-key
```

## 📚 API Documentation

### Base URL

```
http://localhost:8082/api/v1
```

### Authentication Endpoints

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "driver@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "jwt-token-here",
  "userId": 1
}
```

### Driver Endpoints

#### Register Driver
```http
POST /api/v1/drivers/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "password": "password123",
  "licenseNumber": "DL123456",
  "vehicleNumber": "ABC123",
  "vehicleType": "SEDAN"
}
```

**Response:** `201 Created`
```json
{
  "driverId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "licenseNumber": "DL123456",
  "vehicleNumber": "ABC123",
  "vehicleType": "SEDAN",
  "totalRides": 0,
  "available": false
}
```

#### Get Driver
```http
GET /api/v1/drivers/{driverId}
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "licenseNumber": "DL123456",
  "vehicleNumber": "ABC123",
  "vehicleType": "SEDAN",
  "totalRides": 5,
  "latitude": 40.7128,
  "longitude": -74.0060,
  "available": true
}
```

#### Update Location
```http
PUT /api/v1/drivers/{driverId}/location
Authorization: Bearer {token}
Content-Type: application/json

{
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "latitude": 40.7128,
  "longitude": -74.0060,
  "status": "ACTIVE",
  "message": "Driver location updated successfully"
}
```

#### Update Availability
```http
PUT /api/v1/drivers/{driverId}/availability
Authorization: Bearer {token}
Content-Type: application/json

{
  "available": true
}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "available": true,
  "message": "Driver availability updated successfully"
}
```

#### Get Available Drivers
```http
GET /api/v1/drivers/available?latitude=40.7128&longitude=-74.0060&radiusKm=10.0
Authorization: Bearer {token}
```

**Query Parameters:**
- `latitude` (required): Latitude coordinate
- `longitude` (required): Longitude coordinate
- `radiusKm` (optional): Search radius in kilometers (default: 10.0)

**Response:** `200 OK`
```json
[
  {
    "driverId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "available": true
  }
]
```

### Ride Management Endpoints

#### Accept Ride
```http
POST /api/v1/drivers/{driverId}/accept-ride/{rideId}
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "rideId": 100,
  "message": "Driver accepted the ride successfully"
}
```

#### Start Ride
```http
POST /api/v1/drivers/{driverId}/start-ride/{rideId}
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "rideId": 100,
  "message": "Driver started the ride successfully"
}
```

#### Complete Ride
```http
POST /api/v1/drivers/{driverId}/complete-ride
Authorization: Bearer {token}
Content-Type: application/json

{
  "rideId": 100,
  "actualDistanceKm": 10.5,
  "actualFare": 250.0
}
```

**Response:** `200 OK`
```json
{
  "driverId": 1,
  "rideId": 100,
  "actualDistanceKm": 10.5,
  "actualFare": 250.0,
  "message": "Ride completed successfully"
}
```

### Internal Endpoints

#### Notify Driver (Internal)
```http
POST /api/v1/internal/drivers/{driverId}/notify-ride/{rideId}
```

**Response:** `200 OK`

## 📁 Project Structure

```
driver-service/
├── src/
│   ├── main/
│   │   ├── java/com/zeta/driver_service/
│   │   │   ├── client/              # External service clients
│   │   │   │   ├── NotificationServiceClient.java
│   │   │   │   └── RiderServiceClient.java
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── AsyncConfig.java
│   │   │   │   ├── CorrelationIdFilter.java
│   │   │   │   ├── RateLimitConfig.java
│   │   │   │   ├── RestClientConfig.java
│   │   │   │   ├── RetryConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── DriverAuthController.java
│   │   │   │   ├── DriverController.java
│   │   │   │   └── InternalDriverController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── enums/               # Enumerations
│   │   │   ├── exception/           # Exception handlers
│   │   │   ├── interceptor/         # Request interceptors
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── security/            # Security configuration
│   │   │   ├── service/             # Business logic
│   │   │   └── util/                # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway migrations
│   └── test/                         # Unit tests
├── helm-chart/                       # Kubernetes Helm charts
├── Dockerfile                        # Docker image definition
├── pom.xml                           # Maven configuration
└── README.md                         # This file
```

## 🗄 Database Schema

### Tables

1. **drivers**
   - Stores driver information (name, email, phone, license, vehicle details)
   - Tracks total rides and status

2. **driver_availability**
   - Manages driver availability status
   - Tracks current ride assignment

3. **driver_locations**
   - Stores real-time location data
   - Includes latitude, longitude, heading, and speed

4. **driver_earnings**
   - Records earnings per ride
   - Links to driver and ride

### Relationships

- `driver_availability.driver_id` → `drivers.driver_id`
- `driver_locations.driver_id` → `drivers.driver_id`
- `driver_earnings.driver_id` → `drivers.driver_id`

## 🏃 Running the Application

### Local Development

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

### Using Docker

```bash
# Build Docker image
docker build -t driver-service:latest .

# Run container
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/driver_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=admin \
  driver-service:latest
```

### Health Check

```bash
curl http://localhost:8082/actuator/health
```

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=DriverControllerTest
```

### Test Coverage

The project uses JUnit 5 and Mockito for unit testing. Test coverage includes:

- ✅ Controller layer tests
- ✅ Service layer tests
- ✅ Exception handler tests
- ✅ Integration tests

### Test Structure

Tests are located in `src/test/java/com/zeta/driver_service/` and follow the same package structure as the main code.

## 🐳 Docker

### Build Image

```bash
docker build \
  --build-arg app=driver-service \
  --build-arg version=0.0.1-SNAPSHOT \
  --build-arg lastCommitHash=$(git rev-parse --short HEAD) \
  --build-arg lastCommitAuthorEmail=$(git log -1 --pretty=format:'%ae') \
  -t driver-service:latest .
```

### Docker Compose (Example)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: driver_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"

  driver-service:
    build: .
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/driver_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: admin
    depends_on:
      - postgres
```

## ☸️ Kubernetes Deployment

### Using Helm

```bash
# Install
helm install driver-service ./helm-chart

# Upgrade
helm upgrade driver-service ./helm-chart

# Uninstall
helm uninstall driver-service
```

### Configuration

Update `helm-chart/values.yaml` for your environment:

- Image repository and tag
- Resource limits
- Environment variables
- Service configuration

## 📊 Monitoring

### Actuator Endpoints

- **Health**: `http://localhost:8082/actuator/health`
- **Info**: `http://localhost:8082/actuator/info`
- **Metrics**: `http://localhost:8082/actuator/metrics`
- **Prometheus**: `http://localhost:8082/actuator/prometheus`

### Logging

Logs are written to:
- Console (structured JSON format)
- File: `logs/driver-service.log`

Log format includes correlation IDs for request tracing.

## 🔒 Security

### Authentication

- JWT-based authentication
- Token expiration: 24 hours (configurable)
- Secure password hashing with BCrypt

### Rate Limiting

- Configurable rate limits per endpoint
- Prevents abuse and ensures fair usage

### Validation

- Request validation using Jakarta Bean Validation
- Input sanitization
- SQL injection prevention via JPA

### Best Practices

- Non-root user in Docker containers
- Environment-based configuration
- Secrets management via Vault (production)
- HTTPS in production (configure at load balancer)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is part of the Zeta ride-booking system.

## 👥 Contact

For questions or support, please contact the development team.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2026
