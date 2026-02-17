# Rider Service

A microservice for managing riders, rides, and payments in a ride-booking system. This service provides RESTful APIs for rider registration, authentication, ride booking, payment processing, and ride management.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Building and Running](#building-and-running)
- [Docker](#docker)
- [Deployment](#deployment)
- [Project Structure](#project-structure)
- [Monitoring and Metrics](#monitoring-and-metrics)
- [Security](#security)

## Overview

The Rider Service is a Spring Boot-based microservice that handles:
- **Rider Management**: Registration, authentication, and profile management
- **Ride Management**: Booking, cancellation, status updates, and ride history
- **Payment Processing**: Payment creation, processing, and history
- **Integration**: Communication with Driver Service and Notification Service

## Features

- **Rider Registration & Authentication**
  - User registration with email and phone validation
  - JWT-based authentication
  - Password encryption

- **Ride Management**
  - Book rides with pickup and drop locations
  - Find nearby available drivers
  - Cancel rides with reason tracking
  - State machine-based ride status management
  - Ride history retrieval

- **Payment Processing**
  - Multiple payment methods (Credit Card, Debit Card, Cash)
  - Payment status tracking
  - Transaction history

- **Security**
  - JWT token-based authentication
  - Role-based access control
  - Rate limiting
  - Input validation

- **Observability**
  - Prometheus metrics
  - Health checks
  - Structured logging with correlation IDs

- **Resilience**
  - Retry mechanisms for external service calls
  - Optimistic locking for concurrent updates
  - Idempotency support

## 🛠 Tech Stack

- **Framework**: Spring Boot 6.x
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Metrics**: Micrometer, Prometheus
- **Logging**: Log4j2
- **Migration**: Flyway
- **Containerization**: Docker
- **Orchestration**: Kubernetes (Helm Charts)

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (or use Maven Wrapper)
- PostgreSQL 12+
- Docker (optional, for containerized deployment)
- Kubernetes (optional, for deployment)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd rider-service
```

### 2. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE rider_db;
```

### 3. Configure Application

Update `src/main/resources/application.properties` with your database credentials and service URLs:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rider_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Run the Application

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

Or using Maven:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

## Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/rider_db
spring.datasource.username=postgres
spring.datasource.password=admin

# JWT
jwt.secret=YourSecretKey
jwt.expiration=86400000

# Pricing
pricing.base-fare=50.0
pricing.per-km-rate=10.0

# Driver Search
driver.search-radius-km=10.0

# External Services
services.driver-service.url=http://localhost:8082
services.notification-service.url=http://localhost:8083
```

### Environment Variables

You can override properties using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/rider_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=admin
```

## API Documentation

### Authentication

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful"
}
```

### Rider Management

#### Register Rider
```http
POST /api/v1/riders/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "password": "password123"
}
```

#### Get Rider
```http
GET /api/v1/riders/{riderId}
Authorization: Bearer {token}
```

### Ride Management

#### Book Ride
```http
POST /api/v1/rides/book
Authorization: Bearer {token}
Content-Type: application/json

{
  "riderId": 1,
  "pickupLatitude": 40.7128,
  "pickupLongitude": -74.0060,
  "pickupAddress": "123 Main St",
  "dropLatitude": 40.7589,
  "dropLongitude": -73.9851,
  "dropAddress": "456 Park Ave"
}
```

#### Get Ride
```http
GET /api/v1/rides/{rideId}
Authorization: Bearer {token}
```

#### Cancel Ride
```http
POST /api/v1/rides/{rideId}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "cancellationReason": "Changed my mind"
}
```

#### Get Nearby Cabs
```http
GET /api/v1/rides/nearby-cabs?latitude=40.7128&longitude=-74.0060&radiusKm=5.0
Authorization: Bearer {token}
```

#### Get Rides by Rider
```http
GET /api/v1/rides/rider/{riderId}
Authorization: Bearer {token}
```

### Payment Management

#### Process Payment
```http
POST /api/v1/payments/process
Authorization: Bearer {token}
Content-Type: application/json

{
  "rideId": 100,
  "paymentMethod": "CREDIT_CARD"
}
```

#### Get Payment by Ride
```http
GET /api/v1/payments/ride/{rideId}
Authorization: Bearer {token}
```

#### Get Payments by Rider
```http
GET /api/v1/payments/rider/{riderId}
Authorization: Bearer {token}
```

### Internal APIs

#### Update Ride Status (Internal)
```http
PUT /api/v1/internal/rides/{rideId}/status
Content-Type: application/json

{
  "status": "STARTED",
  "startedAt": "2024-01-10T10:00:00"
}
```

#### Get Ride (Internal)
```http
GET /api/v1/internal/rides/{rideId}
```

## Database Schema

### Tables

#### Riders
- `rider_id` (Primary Key)
- `name`, `email`, `phone`
- `password_hash`
- `total_rides`
- `status` (ACTIVE, INACTIVE)
- `created_at`, `updated_at`

#### Rides
- `ride_id` (Primary Key)
- `rider_id` (Foreign Key)
- `driver_id`
- `pickup_latitude`, `pickup_longitude`, `pickup_address`
- `drop_latitude`, `drop_longitude`, `drop_address`
- `status` (REQUESTED, DRIVER_ASSIGNED, STARTED, COMPLETED, CANCELLED, etc.)
- `fare_amount`, `distance_km`
- `requested_at`, `assigned_at`, `started_at`, `completed_at`, `cancelled_at`
- `cancellation_reason`
- `version` (for optimistic locking)
- `idempotency_key`

#### Payments
- `payment_id` (Primary Key)
- `ride_id` (Foreign Key)
- `rider_id` (Foreign Key)
- `amount`
- `payment_method` (CREDIT_CARD, DEBIT_CARD, CASH)
- `payment_status` (PENDING, COMPLETED, FAILED)
- `transaction_id`
- `payment_date`
- `created_at`

Database migrations are managed using Flyway. The initial schema is defined in `src/main/resources/db/migration/V1.0.1__create_initial_tables.sql`.

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=RideControllerTest
```

### Run Tests with Coverage

```bash
./mvnw test jacoco:report
```

### Test Coverage

The project includes comprehensive unit tests for:
- Controllers (Auth, Rider, Ride, Payment, InternalRide)
- Services (Rider, Ride, Payment, Authorization, Metrics, Validation)
- Exception Handlers
- State Machine

## Building and Running

### Build the Application

```bash
./mvnw clean package
```

This creates a JAR file in the `target/` directory.

### Run the JAR

```bash
java -jar target/zea-opc-b05-ridebooking-rider-service-soumyami-0.0.1-SNAPSHOT.jar
```

### Helm Chart Configuration

Key values in `helm-chart/values.yaml`:
- Replica count
- Resource limits
- Environment variables
- Service configuration
- Horizontal Pod Autoscaler (HPA)

## Project Structure

```
rider-service/
├── src/
│   ├── main/
│   │   ├── java/com/zeta/rider_service/
│   │   │   ├── client/          # External service clients
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Enumerations
│   │   │   ├── exception/       # Custom exceptions and handlers
│   │   │   ├── interceptor/     # HTTP interceptors
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business logic
│   │   │   ├── statemachine/   # State machine for ride status
│   │   │   └── util/            # Utility classes and mappers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Test classes
├── helm-chart/                   # Kubernetes Helm charts
├── Dockerfile                    # Docker image definition
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

## Monitoring and Metrics

### Health Check

```http
GET /actuator/health
```

### Metrics

```http
GET /actuator/metrics
```

### Prometheus Endpoint

```http
GET /actuator/prometheus
```

### Available Metrics

- `rides.created` - Total rides created
- `rides.completed` - Total rides completed
- `rides.cancelled` - Total rides cancelled
- `assignments.failed` - Failed driver assignments
- `validation.failures` - Validation failures

## Security

### Authentication

- JWT-based authentication
- Token expiration: 24 hours (configurable)
- Password encryption using BCrypt

### Authorization

- Role-based access control
- Rider ownership verification
- Driver-specific endpoints

### Rate Limiting

- Configurable rate limits per endpoint
- Prevents abuse and ensures fair usage

### Input Validation

- Bean validation on all request DTOs
- Custom validation for business rules

## Ride Status Flow

The ride status follows a state machine pattern:

```
REQUESTED → DRIVER_ASSIGNED → DRIVER_ARRIVED → STARTED → COMPLETED
                ↓                    ↓              ↓
            CANCELLED            CANCELLED      CANCELLED
```

Invalid state transitions are prevented by the `RideStateMachine`.

## External Service Integration

### Driver Service

- **Endpoint**: `GET /api/v1/drivers/available`
- **Purpose**: Find available drivers near a location
- **Retry**: Configured with exponential backoff

### Notification Service

- **Endpoints**: 
  - `POST /api/v1/notifications/send` - Send notifications
  - `POST /api/v1/notifications/ride-accepted` - Ride accepted notification
  - `POST /api/v1/notifications/ride-completed` - Ride completed notification
- **Async**: Notifications sent asynchronously

## Logging

- Structured logging with Log4j2
- Correlation ID tracking for request tracing
- Log files: `logs/rider-service.log`
- Log levels configurable per package

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify PostgreSQL is running
   - Check database credentials in `application.properties`
   - Ensure database exists

2. **Port Already in Use**
   - Change `server.port` in `application.properties`
   - Or stop the process using port 8081

3. **JWT Token Invalid**
   - Check `jwt.secret` configuration
   - Verify token expiration time

4. **External Service Unavailable**
   - Verify Driver Service and Notification Service URLs
   - Check network connectivity
   - Review retry configuration

## License

This project is part of the Zeta ride-booking system.

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Ensure all tests pass
5. Submit a pull request

## Support

For issues and questions, please contact the development team or create an issue in the repository.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2026 by Soumya