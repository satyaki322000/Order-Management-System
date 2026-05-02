# Inventory Management System

A Spring Boot backend service for managing product inventory.
It supports direct inventory operations through REST APIs and asynchronous stock updates through Kafka order events.

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Apache Kafka
- Maven

## Features

- Check inventory and reduce stock during order processing
- Increase stock manually
- Decrease stock manually
- Persist inventory data in an H2 file-based database
- Consume Kafka events from `order-topic`
- Prevent duplicate Kafka event processing using `eventId`
- Global exception handling for missing products

## Project Structure

```text
src/main/java/com/example/inventorymanagementsystem
├── config          # Kafka producer, consumer, and error-handler configuration
├── controller      # REST endpoints
├── dto             # Kafka event payload classes
├── entity          # JPA entities
├── exception       # Custom exceptions and global handler
├── kafka           # Kafka consumer service
├── repository      # Spring Data JPA repositories
└── service         # Business logic
```

## Configuration

Main configuration is in `src/main/resources/application.yaml`.

### Default settings

- App name: `InventoryManagementSystem`
- Server port: `8082`
- H2 DB URL: `jdbc:h2:file:./data/inventorydb`
- H2 console: `http://localhost:8082/h2-console`
- Kafka bootstrap server: `localhost:9092`
- Kafka consumer group: `inventory-group`

## Prerequisites

Make sure the following are installed and running:

- Java 21+
- Maven or the included Maven wrapper
- Kafka running on `localhost:9092`

## Run the Application

Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or with Maven:

```bash
mvn spring-boot:run
```

The service will start on:

```text
http://localhost:8082
```

## REST APIs

Base path: `/inventory`

### 1. Check and reduce inventory

Reduces stock only if enough quantity is available.

```http
POST /inventory/check?product={productName}&qty={quantity}
```

Example:

```bash
curl -X POST "http://localhost:8082/inventory/check?product=Laptop&qty=2"
```

Response:

- `true` -> stock available and reduced
- `false` -> insufficient stock

### 2. Increase stock

```http
POST /inventory/increase?product={productName}&qty={quantity}
```

Example:

```bash
curl -X POST "http://localhost:8082/inventory/increase?product=Laptop&qty=5"
```

### 3. Decrease stock

```http
POST /inventory/decrease?product={productName}&qty={quantity}
```

Example:

```bash
curl -X POST "http://localhost:8082/inventory/decrease?product=Laptop&qty=1"
```

## Kafka Event Processing

The service listens to the Kafka topic:

```text
order-topic
```

### Supported event types

- `CREATE`
  Decreases inventory by `quantity`

- `DELETE`
  Increases inventory by `quantity`

- `UPDATE`
  Compares `quantity` with `oldQuantity`
  - if new quantity is higher, decreases stock by the difference
  - if new quantity is lower, increases stock by the difference

### Order event payload

```json
{
  "eventId": "evt-101",
  "eventType": "CREATE",
  "productName": "Laptop",
  "quantity": 2,
  "oldQuantity": 0
}
```

### Duplicate Event Protection

Processed Kafka events are stored using `eventId`.
If the same event is received again, it is skipped.

## Database

The application uses an H2 file-based database stored under:

```text
./data/inventorydb
```

### H2 Console

Access the console at:

```text
http://localhost:8082/h2-console
```

Use:

- JDBC URL: `jdbc:h2:file:./data/inventorydb`
- Username: `sa`
- Password: leave empty

## Error Handling

If a product does not exist, the service returns a `404 Not Found` response.

Example response:

```json
{
  "timestamp": "2026-05-03T10:00:00",
  "status": 404,
  "message": "Product not found: Laptop",
  "service": "INVENTORY-SERVICE"
}
```

## Testing

Run tests with:

```bash
./mvnw test
```

## Notes

- Products must already exist in the `Inventory` table before calling the REST APIs.
- There is currently no API in this service to create a new product entry.
- Kafka should be available if you want event-driven inventory updates.
- The current test suite only verifies Spring context startup.

## Future Improvements

- Add API to create and list inventory items
- Add request validation
- Add unit and integration tests
- Add Swagger/OpenAPI documentation
- Add Docker support
- Add environment-based configuration
