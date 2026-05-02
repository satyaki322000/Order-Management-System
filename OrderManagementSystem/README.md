# Order Management System

A Spring Boot backend service for managing orders with JWT-based authentication, role-based authorization, H2 database persistence, and Kafka event publishing.

## Features

- JWT-based login
- Role-based access control for `ADMIN` and `USER`
- Create, read, update, and delete order APIs
- H2 file-based database
- Kafka producer for order lifecycle events
- Optimistic locking on orders with `@Version`
- Global exception handling for validation and service errors

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring Web MVC
- Spring Security
- Spring Data JPA
- H2 Database
- Apache Kafka
- Lombok
- JJWT
- Resilience4j

## Project Structure

```text
src/main/java/com/example/ordermanagementsystem
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
└── service
```

## Configuration

Application settings are defined in `src/main/resources/application.yaml`.

Default configuration:

- Server port: `8081`
- H2 JDBC URL: `jdbc:h2:file:./data/ordersdb`
- H2 console path: `/h2-console`
- Kafka bootstrap server: `localhost:9092`

## Prerequisites

- JDK 21 or later
- Kafka running on `localhost:9092` if you want event publishing to succeed

## Run the Application

Start the service:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

The application starts on:

```text
http://localhost:8081
```

## Authentication

### Login

Endpoint:

```http
POST /auth/login
```

Request body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response:

```text
<jwt-token>
```

Use the token in authenticated requests:

```http
Authorization: Bearer <jwt-token>
```

## API Endpoints

Base path:

```text
/api/orders
```

### Create Order

```http
POST /api/orders
```

Role required: `ADMIN`

Request body:

```json
{
  "productName": "Laptop",
  "quantity": 2,
  "price": 50000
}
```

### Get All Orders

```http
GET /api/orders
```

Roles allowed: `USER`, `ADMIN`

### Update Order

```http
PUT /api/orders/{id}
```

Role required: `ADMIN`

Request body:

```json
{
  "quantity": 3,
  "price": 52000
}
```

Note: the current implementation updates only `quantity` and `price`.

### Delete Order

```http
DELETE /api/orders/{id}
```

Role required: `ADMIN`

## Order Lifecycle Events

Order actions publish Kafka messages to:

```text
order-topic
```

Published event types:

- `CREATE`
- `UPDATE`
- `DELETE`

Behavior:

- Creating an order sets its status to `CREATED` and publishes a `CREATE` event
- Updating an order publishes an `UPDATE` event with old and new quantity data
- Deleting an order publishes a `DELETE` event before removal

## Database

The service uses an H2 file database stored at:

```text
./data/ordersdb
```

H2 console:

- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:file:./data/ordersdb`
- Username: `sa`
- Password: leave blank

## Security Rules

- `/auth/**` and `/h2-console/**` are public
- `GET /api/orders/**` requires `USER` or `ADMIN`
- `POST /api/orders/**` requires `ADMIN`
- `PUT /api/orders/**` requires `ADMIN`
- `DELETE /api/orders/**` requires `ADMIN`

## Notes

- There is no registration endpoint
- Users must already exist in the `users` table for login to work
- Passwords currently use `NoOpPasswordEncoder`, which is only suitable for local development
- The JWT secret is hardcoded in the application
- Kafka send failures are logged, but order operations still continue
- The test suite currently contains only a basic Spring context load test
- Validation annotations are present on `Order`, but the current dependencies do not include a Jakarta Bean Validation provider implementation

## Suggested Improvements

- Seed default users or add a registration endpoint
- Replace `NoOpPasswordEncoder` with BCrypt
- Move JWT and Kafka configuration to environment variables
- Add Swagger or OpenAPI documentation
- Add controller and integration tests
- Add Docker support
- Add a Bean Validation implementation such as `spring-boot-starter-validation`
