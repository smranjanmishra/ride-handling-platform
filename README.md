# RideHandling Platform

A Spring Boot microservices backend for a ride-booking workflow. The platform is split into three services:

- `rider-service`: rider registration/authentication, ride booking and lifecycle, payments
- `driver-service`: driver registration/authentication, availability/location, ride acceptance/completion
- `notification-service`: notification persistence and delivery for ride events

Each service is independently deployable and communicates over REST using service URLs configured in `application.properties`.

## Architecture

### Services and ports (local defaults)

| Service | Folder | Default port |
|---|---|---:|
| Rider Service | `rider-service/` | 8081 |
| Driver Service | `driver-service/` | 8082 |
| Notification Service | `notification-service/` | 8083 |

### Service-to-service calls (high level)

- `rider-service` calls `driver-service` to fetch available drivers and trigger driver notifications.
- `rider-service` calls `notification-service` to send ride-related notifications.
- `driver-service` calls `rider-service` internal APIs to update ride status.
- `driver-service` calls `notification-service` to send notifications.
- `notification-service` calls `rider-service` internal APIs to fetch ride details.

## Tech stack

- Java 17
- Spring Boot 3.x
- Maven (each service includes Maven Wrapper: `mvnw` / `mvnw.cmd`)
- PostgreSQL (runtime database; schema managed via Flyway in services that include migrations)
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- Bucket4j rate limiting (interceptors)
- Observability: Spring Actuator, Micrometer, Prometheus
- Logging: Log4j2 with correlation ID pattern (see each service `application.properties`)

## Repository layout

```
RideHandling Platform/
  driver-service/
  rider-service/
  notification-service/
```

Each service is a standalone Maven project with its own `pom.xml`, `src/`, and README.

## Prerequisites

### Required

- Java 17 installed
- `JAVA_HOME` set to your JDK install directory
- PostgreSQL installed and running

### Optional

- Docker (container builds and local containers)
- Kubernetes/Helm (charts exist inside service folders)

## Quick start (local development)

### 1) Create databases

Create three PostgreSQL databases:

```sql
CREATE DATABASE rider_db;
CREATE DATABASE driver_db;
CREATE DATABASE notification_db;
```

Defaults in the repo `application.properties` use:

- username: `postgres`
- password: `admin`

Change these for your environment (recommended) and override using environment variables or by editing each service’s `application.properties`.

### 2) Configure service URLs

Defaults are already wired for local ports:

- `rider-service` points to `driver-service` at `http://localhost:8082` and `notification-service` at `http://localhost:8083`
- `driver-service` points to `rider-service` at `http://localhost:8081` and `notification-service` at `http://localhost:8083`
- `notification-service` points to `rider-service` at `http://localhost:8081` and `driver-service` at `http://localhost:8082`

### 3) Build and run

Open three terminals and run from each service directory.

#### Rider Service

```bash
cd rider-service
./mvnw clean package
./mvnw spring-boot:run
```

#### Notification Service

```bash
cd notification-service
./mvnw clean package
./mvnw spring-boot:run
```

#### Driver Service

```bash
cd driver-service
./mvnw clean package
./mvnw spring-boot:run
```

### Windows notes

Use the wrapper script:

```powershell
cd rider-service
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

If you see `JAVA_HOME environment variable is not defined correctly`, set it to your JDK path and restart your terminal.

## Configuration

Each service reads configuration from:

- `src/main/resources/application.properties`
- environment variables (override any Spring property)

### Common properties (by service)

#### Database

All services use PostgreSQL by default:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Local defaults (as committed):

- Rider: `jdbc:postgresql://localhost:5432/rider_db`
- Driver: `jdbc:postgresql://localhost:5432/driver_db`
- Notification: `jdbc:postgresql://localhost:5432/notification_db`

#### JWT

Each service has its own JWT settings in `application.properties`:

- `jwt.secret`
- `jwt.expiration`

Recommendation: override secrets via environment variables in non-local environments.

#### Service URLs

- Rider:
  - `services.driver-service.url`
  - `services.notification-service.url`
- Driver:
  - `services.rider-service.url`
  - `services.notification-service.url`
- Notification:
  - `services.rider-service.url`
  - `services.driver-service.url`

#### Rider pricing and driver search

In `rider-service`:

- `pricing.base-fare`
- `pricing.per-km-rate`
- `driver.search-radius-km`

#### Email (Notification Service)

In `notification-service` (optional; can be disabled by configuration):

- `spring.mail.host`
- `spring.mail.port`
- `spring.mail.username`
- `spring.mail.password`

Note: `management.health.mail.enabled=false` is set by default to prevent startup health failures when mail isn’t configured.

## API overview

The platform exposes REST APIs. A concise summary is below; for full examples and payloads, see the service READMEs:

- `rider-service/README.md`
- `driver-service/README.md`
- `notification-service/README.md`

### Rider Service (port 8081)

- Authentication: `POST /api/v1/auth/login`
- Riders: `POST /api/v1/riders/register`, `GET /api/v1/riders/{riderId}`
- Rides: `POST /api/v1/rides/book`, `GET /api/v1/rides/{rideId}`, `POST /api/v1/rides/{rideId}/cancel`
- Nearby drivers: `GET /api/v1/rides/nearby-cabs?latitude=...&longitude=...&radiusKm=...`
- Payments: `POST /api/v1/payments/process`, `GET /api/v1/payments/ride/{rideId}`, `GET /api/v1/payments/rider/{riderId}`
- Internal: `PUT /api/v1/internal/rides/{rideId}/status`, `GET /api/v1/internal/rides/{rideId}`

### Driver Service (port 8082)

- Authentication: `POST /api/v1/auth/login`
- Drivers: `POST /api/v1/drivers/register`, `GET /api/v1/drivers/{driverId}`
- Availability: `PUT /api/v1/drivers/{driverId}/availability`
- Location: `PUT /api/v1/drivers/{driverId}/location`
- Nearby available drivers: `GET /api/v1/drivers/available?latitude=...&longitude=...&radiusKm=...`
- Ride actions: `POST /api/v1/drivers/{driverId}/accept-ride/{rideId}`, `POST /api/v1/drivers/{driverId}/start-ride/{rideId}`, `POST /api/v1/drivers/{driverId}/complete-ride`

### Notification Service (port 8083)

Routes are service-specific; see `notification-service/README.md` for exact paths and payloads.

## Observability

All services expose Actuator endpoints and Prometheus metrics by default:

- Health: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`
- Prometheus: `GET /actuator/prometheus`

## Logs

Each service writes logs to a file (see each service `application.properties`), for example:

- `logs/rider-service.log`
- `logs/driver-service.log`
- `logs/notification-service.log`