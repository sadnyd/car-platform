# Car Platform – Microservices Architecture


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
