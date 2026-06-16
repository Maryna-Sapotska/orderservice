# Order Service

Microservice for managing customer orders in a distributed system.

The service handles order creation, retrieval, filtering, updates, and deletion, and integrates with external user service to enrich order data.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web / REST
  Spring Cloud OpenFeign
  Spring Cloud CircuitBreaker (Resilience4j)
- PostgreSQL
- Liquibase
- Docker
- Docker Compose
- Testcontainers (PostgreSQL + WireMock)
- MockMvc
- JUnit 5
- Mockito
- MapStruct
- Maven
- Swagger/OpenAPI

---

# Features

## Order Management

- Create order
- Get order by id
- Get all orders with pagination
- Get orders by user id
- Filter orders by:
    - status
    - created date range
- Update order status (PATCH)
- Soft delete order

## Business Logic

- Order total price calculation
- Order items aggregation
- Enrichment with user data from external service
- Fallback handling when user service is unavailable
- Soft delete with Hibernate (@SQLDelete, @Where)

## Additional Features

- Liquibase migrations
- JPA auditing
- Specifications filtering
- Validation
- Exception handling
- Feign Client integration
- CircuitBreaker with fallback (Resilience4j)
- MapStruct mapping
- Unit tests
- Integration tests (Testcontainers + WireMock)
- Docker support
- CI Pipeline with GitHub Actions

---

# Database

## Tables

### orders

| Column      | Type      |
| ----------- | --------- |
| id          | bigint    |
| user_id     | bigint    |
| status      | varchar   |
| total_price | decimal   |
| deleted     | boolean   |
| created_at  | timestamp |
| updated_at  | timestamp |

### order_items

| Column     | Type      |
| ---------- | --------- |
| id         | bigint    |
| order_id   | bigint    |
| item_id    | bigint    |
| quantity   | int       |
| created_at | timestamp |
| updated_at | timestamp |

### items

| Column     | Type      |
| ---------- | --------- |
| id         | bigint    |
| name       | varchar   |
| price      | decimal   |
| created_at | timestamp |
| updated_at | timestamp |

---

# Requirements

- Java 21
- Maven
- Docker Desktop

---

# Run Application Locally

## 1. Clone repository

```bash
git clone <repository-url>
cd orderservice
```

## 2. Start infrastructure

docker compose up -d

This starts:

- PostgreSQL
- Order Service

## Run application

mvn spring-boot:run

Application will start on:

http://localhost:8082

# Run With Docker

## Build application

mvn clean package

## Build Docker image

docker build -t orderservice .

## Start containers

docker compose up --build

--- 

# Spring Profiles

## test

Used for integration tests:

- PostgreSQL via Testcontainers
- WireMock for external users-service
- Liquibase enabled
- Hibernate validation (ddl-auto: validate)

--- 

# API Endpoints

## Orders

| Method | Endpoint                | Description                          |
| ------ | ----------------------- | ------------------------------------ |
| POST   | `/orders`               | Create order                         |
| GET    | `/orders/{id}`          | Get order by id                      |
| GET    | `/orders`               | Get all orders (filter + pagination) |
| GET    | `/orders/user/{userId}` | Get orders by user id                |
| PATCH  | `/orders/{id}`          | Update order status                  |
| DELETE | `/orders/{id}`          | Delete order (soft delete)           |

---

## Example Request

{
  "userId": 1,
  "items": [
    {
      "itemId": 10,
      "quantity": 2
    }
  ]
}

## Example Response

{
  "id": 1,
  "status": "CREATED",
  "totalPrice": 2000,
  "createdAt": "2026-06-07T10:00:00",
  "items": [
    {
      "itemId": 10,
      "itemName": "Laptop",
      "itemPrice": 1000,
      "quantity": 2
    }
  ],
  "user": {
    "id": 1,
    "name": "John",
    "surname": "Doe",
    "email": "john@test.com",
    "active": true
  }
}

---

## External Services

### Users Service

Integrated via Feign Client:

- URL: services.users-service.url
- Endpoint: /users/{userId}

Features:

- Request forwarding
- CircuitBreaker protection
- Fallback when service is unavailable

---

# Testing

## Unit Tests

Service layer covered with unit tests using:

- JUnit 5
- Mockito

## Integration Tests

Integration tests implemented with:

- Spring Boot Test
- Testcontainers (PostgreSQL)
- WireMock (external service mocking)

Run tests:

mvn test

---

# CI Pipeline

GitHub Actions pipeline includes:

- Build
- Testing
- SonarQube analysis
- Docker image build

Pipeline configuration located in:

.github/workflows/ci.yml

---

# Author

Marina Sapotska