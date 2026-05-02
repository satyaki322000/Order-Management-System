# OMS Backend

This repository contains two Spring Boot backend services that work together for order and inventory management:

- `OrderManagementSystem`
- `InventoryManagementSystem`

The order service manages authenticated order operations and publishes Kafka events. The inventory service consumes those events and updates stock levels.

## Architecture

```text
Client
  |
  v
OrderManagementSystem (port 8081)
  - JWT login
  - role-based order APIs
  - H2 orders database
  - publishes order events to Kafka
  |
  v
Kafka topic: order-topic
  |
  v
InventoryManagementSystem (port 8082)
  - inventory APIs
  - H2 inventory database
  - consumes order events
  - adjusts stock and skips duplicate events
```

## Services

### 1. OrderManagementSystem

Location: `OrderManagementSystem/`

Responsibilities:

- Authenticate users with JWT
- Enforce role-based access for order APIs
- Create, list, update, and delete orders
- Persist orders in H2
- Publish `CREATE`, `UPDATE`, and `DELETE` events to Kafka topic `order-topic`

Default config:

- Port: `8081`
- H2 DB: `jdbc:h2:file:./data/ordersdb`
- H2 console: `http://localhost:8081/h2-console`

Main endpoints:

- `POST /auth/login`
- `GET /api/orders`
- `POST /api/orders`
- `PUT /api/orders/{id}`
- `DELETE /api/orders/{id}`

Security rules:

- `/auth/**` and `/h2-console/**` are public
- `GET /api/orders/**` requires `USER` or `ADMIN`
- `POST`, `PUT`, `DELETE /api/orders/**` require `ADMIN`

Important implementation notes:

- There is no registration endpoint
- Users must already exist in the `users` table
- Passwords use `NoOpPasswordEncoder`
- JWT secret is hardcoded
- Order operations continue even if Kafka publishing fails
- `Order` uses optimistic locking with `@Version`

### 2. InventoryManagementSystem

Location: `InventoryManagementSystem/`

Responsibilities:

- Manage stock quantities for products
- Expose REST APIs for manual stock operations
- Consume order events from Kafka
- Increase or decrease stock based on event type
- Prevent duplicate event handling with `eventId`

Default config:

- Port: `8082`
- H2 DB: `jdbc:h2:file:./data/inventorydb`
- H2 console: `http://localhost:8082/h2-console`
- Kafka consumer group: `inventory-group`

Main endpoints:

- `POST /inventory/check?product={name}&qty={n}`
- `POST /inventory/increase?product={name}&qty={n}`
- `POST /inventory/decrease?product={name}&qty={n}`

Kafka behavior:

- `CREATE` -> decrease inventory by `quantity`
- `DELETE` -> increase inventory by `quantity`
- `UPDATE` -> adjust inventory by the difference between `quantity` and `oldQuantity`

Important implementation notes:

- Products must already exist in the inventory database
- There is no API to create inventory items
- Duplicate Kafka events are skipped using `ProcessedEvent`
- Missing products return `404`

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Data JPA
- Spring Security
- H2 Database
- Apache Kafka
- Lombok
- JJWT
- Resilience4j

## Repository Layout

```text
backend/
├── InventoryManagementSystem/
│   ├── src/
│   ├── data/
│   ├── pom.xml
│   └── README.md
└── OrderManagementSystem/
    ├── src/
    ├── data/
    ├── pom.xml
    └── README.md
```

## Prerequisites

- JDK 21 or later
- Kafka running on `localhost:9092`

Each service includes its own Maven wrapper, so a separate local Maven install is optional.

## How to Run

Start Kafka first.

Then start the services in separate terminals.

### Start OrderManagementSystem

```bash
cd OrderManagementSystem
./mvnw spring-boot:run
```

### Start InventoryManagementSystem

```bash
cd InventoryManagementSystem
./mvnw spring-boot:run
```

## How the Services Work Together

1. A client logs in to `OrderManagementSystem` using `/auth/login`.
2. The client calls order APIs with a bearer token.
3. When an order is created, updated, or deleted, the order service publishes an event to Kafka topic `order-topic`.
4. `InventoryManagementSystem` consumes the event.
5. Inventory is adjusted based on the event type.
6. The event ID is stored so the same event is not processed twice.

## Running Tests

Run tests per service:

```bash
cd OrderManagementSystem
./mvnw test
```

```bash
cd InventoryManagementSystem
./mvnw test
```

Current test coverage is minimal. Both services only include a basic Spring context startup test.

## Databases

Both services use file-based H2 databases under their own `data/` directories:

- `OrderManagementSystem/data/ordersdb`
- `InventoryManagementSystem/data/inventorydb`

H2 console credentials for both:

- Username: `sa`
- Password: empty

## Known Gaps

- No Docker or compose setup
- No seeded users for order login
- No seeded inventory items
- No validation provider dependency despite validation annotations being present
- Several secrets and infrastructure settings are hardcoded
- No end-to-end or integration tests

## Suggested Next Steps

- Add repo-level Docker Compose for Kafka and both services
- Seed default users and inventory items
- Replace `NoOpPasswordEncoder` with BCrypt
- Move JWT and Kafka settings to environment variables
- Add OpenAPI documentation
- Add service-to-service integration tests
