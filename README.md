# Car Platform – Microservices Architecture


**Language:** Java 17 | **Build Tool:** Maven | **Framework:** Spring Boot 3.x+

This is a production-ready microservices system for managing car catalog, inventory, orders, and users.

---

## 🚀 Quick Start


Services:
- **API Gateway** – Entry point for all client requests and routing
- **Car Catalog Service** – Manages car master data
- **Inventory Service** – Manages stock and availability
- **Order Service** – Manages order lifecycle
- **User Service** – Manages user identity and roles

---

## Bounded Contexts & Data Ownership

Each service owns its domain data and acts as the **single source of truth**
for that data. No service is allowed to modify another service’s data.

- **Car Catalog Service (8081)**  
  Owns all car-related master data such as brand, model, variant,
  specifications, and pricing.  
  It is the single source of truth for car information.

- **Inventory Service (8082)**  
  Owns availability, stock levels, reservations, and location-based
  inventory.  
  No other service can modify inventory state.

- **Order Service (8083)**  
  Owns the purchase lifecycle, including order creation, status
  transitions, and cancellations.  
  It does not manage car or inventory data directly and references them
  only by identifiers.

- **User Service (8084)**  
  Owns customer identity, roles, and profile information.  
  Authentication and authorization depend on this service.

---

## Data Boundary Rules

- Each service owns and modifies **only its own data**.
- No service directly accesses another service’s database.
- Cross-service communication happens **only via APIs or events**.
- Services reference external data using identifiers, not shared models.
- There are **no shared domain models or shared persistence layers**.

These rules enforce strict microservice isolation and prevent tight coupling.

---

## Technology Stack

- Java 21
- Spring Boot 3.3.0
- Maven
- REST APIs

---

## Service Ports

- api-gateway : 8080
- car-catalog-service : 8081
- inventory-service : 8082
- order-service : 8083
- user-service : 8084

---
| Domain Service | Purpose | Aggregate Root | Identity | Core Attributes | Key States / Enums | Change Frequency | Referenced By | Does NOT Handle |
|---------------|---------|----------------|----------|-----------------|-------------------|-----------------|---------------|----------------|
| **Car Catalog** | Defines car master data | Car | CarId | Brand, Model, Variant, Year, FuelType, Transmission, BasePrice, Description | FuelType, TransmissionType | Low | Inventory, Order | Availability, reservations, orders |
| **Inventory** | Manages stock & availability | InventoryItem | InventoryId, CarId | TotalUnits, AvailableUnits, ReservedUnits, Location | InventoryStatus (AVAILABLE, LOW_STOCK, OUT_OF_STOCK) | High | Order | Pricing, user data, car specs |
| **User** | Owns user identity & role | User | UserId | FullName, Email, Phone | UserRole (CUSTOMER, ADMIN), AccountStatus | Low | Order | Authentication, sessions, tokens |
| **Order** | Controls order lifecycle & coordination | Order | OrderId | UserId, CarId, PriceAtOrderTime, CreatedAt, ReservationExpiry | OrderStatus (CREATED, CONFIRMED, CANCELLED, EXPIRED) | Medium–High | — | Inventory updates, car data, payments |

## How to Run (Local Development)

Each service is started independently using Maven and embedded Tomcat.

### Development Mode (PowerShell)

Run the following command inside each service directory:

```bash
./mvnw spring-boot:run
```

Each service will start on its configured port.

Build / Packaged Mode

```bash
./mvnw clean package
java -jar target/<service-name>.jar
```

## Phase 9 Dockerization

This repository supports full containerized bring-up of all services and isolated databases with a single command.

### What is implemented

- Multi-stage `Dockerfile` for each service (`api-gateway`, `car-catalog-service`, `inventory-service`, `order-service`, `user-service`)
- Dedicated Postgres container per service database
- Internal bridge network for service-to-service DNS resolution
- `docker` Spring profile (`application-docker.yaml`) for all services
- Environment-driven DB and downstream service wiring
- Health checks for DB and all services via `/actuator/health`
- Gateway-only external port exposure (`8080`)

### Files added for Phase 9

- `docker-compose.yml`
- `.env.example`
- `<service>/.dockerignore` for all 5 services
- `<service>/Dockerfile` for all 5 services
- `<service>/src/main/resources/application-docker.yaml` for all 5 services

### One-time setup

1. Copy environment template:

```bash
cp .env.example .env
```

2. Edit `.env` with secure credentials (do not commit).

### Start the full platform

```bash
docker compose --env-file .env up --build -d
```

### Validate platform health

```bash
docker compose --env-file .env ps
curl http://localhost:8080/actuator/health
```

### Logs and observability

```bash
docker compose --env-file .env logs -f api-gateway
docker compose --env-file .env logs -f order-service
```

### End-to-end smoke flow via gateway

- Create catalog car: `POST /catalog`
- Check inventory: `GET /inventory/{carId}`
- Place order: `POST /orders`
- Aggregated flow checks through `api-gateway`

### Failure drills (resilience checks)

```bash
docker compose --env-file .env stop inventory-service
docker compose --env-file .env start inventory-service

docker compose --env-file .env restart order-db
docker compose --env-file .env restart order-service
```

Verify recovery from logs and health endpoints after each drill.

### Shutdown and cleanup

```bash
docker compose --env-file .env down
docker compose --env-file .env down -v
docker image prune -f
```

### Common Docker issues

- **Service unhealthy at startup**: inspect `docker compose logs -f <service>` and ensure DB credentials match `.env`.
- **DB connection errors**: confirm service `DB_HOST` points to container service name (not `localhost`).
- **Port conflict on 8080**: free port 8080 or change gateway host mapping in `docker-compose.yml`.
