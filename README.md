# Order Events

An event-driven order processing service built with Spring Boot and Apache Kafka.
Creating an order persists it and publishes an event; a separate consumer reacts
to that event and updates the order's status — demonstrating asynchronous,
decoupled communication between components.

This is a portfolio project focused on learning and showcasing **event-driven
architecture** with Kafka in a Spring ecosystem.

## Architecture

The flow is split into two independent sides that communicate only through a
Kafka topic:

```
POST /orders ──► OrderService ──► PostgreSQL        (persist the order)
                      │
                      └────────► Kafka topic "orders.created"
                                        │
                                        ▼
                                 @KafkaListener ──► OrderService ──► PostgreSQL
                                 (consumer group        (update status to
                                  "orders-service")      PAYMENT_CONFIRMED)
```

1. The REST endpoint creates an order and returns `201 Created` immediately —
   it does **not** wait for any downstream processing.
2. The same request publishes an `OrderCreatedEvent` to the `orders.created`
   topic, keyed by `orderId`.
3. A consumer, running in a background thread within the same service, reads the
   event and updates the order's status asynchronously.

This separation is the core idea: producing an event and reacting to it are
decoupled in time. The API stays fast and responsive regardless of how long the
downstream processing takes.

## Tech stack

- **Java 21**
- **Spring Boot 4.1** (Web MVC, Data JPA, Validation, Actuator)
- **Spring for Apache Kafka**
- **Apache Kafka 4.1** (KRaft mode — no ZooKeeper)
- **PostgreSQL 18**
- **Docker Compose** (full local stack)
- **JUnit 5 + Mockito** (tests)
- **Maven** (build)

## Getting started

### Prerequisites

- Docker and Docker Compose
- (Optional, for running outside Docker) JDK 21 and Maven

### Run the whole stack with Docker

A single command builds the application image and starts the app, Kafka and
PostgreSQL together:

```bash
docker compose up -d --build
```

The first build takes a few minutes (it downloads the Maven image and compiles
the project). Once running, the API is available at `http://localhost:8080`.

Check that everything is up:

```bash
docker compose ps
```

Stop everything:

```bash
docker compose down
```

(Order data survives restarts via a named volume. Add `-v` to also wipe the
database.)

### Run the app from your IDE

You can run Kafka and PostgreSQL in Docker while running the app locally:

```bash
docker compose up -d postgres kafka
./mvnw spring-boot:run
```

The application defaults to `localhost` for both the database and Kafka, so no
extra configuration is needed. Inside Docker, the `DB_HOST` and `KAFKA_HOST`
environment variables override those defaults to point at the service names.

## API

### Create an order

```
POST /orders
Content-Type: application/json

{
  "customer": "Arnald",
  "amount": 199.90
}
```

Response: `201 Created`

```json
{
  "id": 1,
  "customer": "Arnald",
  "amount": 199.90,
  "status": "CREATED",
  "createdAt": "2026-10-06T16:07:38.453Z"
}
```

The order is returned with status `CREATED`. Shortly after, the consumer updates
it to `PAYMENT_CONFIRMED` in the background. You can observe the change by
querying the database:

```bash
docker exec -it order-events-postgres \
  psql -U orderuser -d orderdb -c "SELECT id, customer, status FROM orders;"
```

Validation is enforced on the request body: `customer` must not be blank and
`amount` must be positive. Invalid requests return `400 Bad Request`.

## Testing

```bash
./mvnw test
```

The suite includes unit tests for the service layer (using Mockito to isolate
the repository and the Kafka producer) and a context-load test.

> Note: the context-load test starts the full application, so Kafka and
> PostgreSQL must be running (`docker compose up -d postgres kafka`) for it to
> pass.

## Design notes

- **Events carry a dedicated DTO, not the JPA entity.** `OrderCreatedEvent` is a
  separate record decoupled from the `Order` entity, so internal persistence
  changes do not leak into the event contract that consumers depend on.
- **Events are named as facts in the past tense** (`OrderCreatedEvent`),
  reflecting something that already happened rather than a command to act.
- **The message key is the `orderId`**, so all events for a given order land on
  the same partition and preserve ordering.
- **Enum stored as string** (`@Enumerated(EnumType.STRING)`) to keep the
  database readable and stable against enum reordering.
- **`BigDecimal` for monetary values** to avoid floating-point rounding errors.

## Known limitations

- **Dual-write problem.** The order is saved to PostgreSQL and the event is
  published to Kafka as two separate writes, without a shared transaction. If
  the publish fails after the save succeeds, the order exists with no event. A
  production-grade solution is the **Transactional Outbox pattern** (persist the
  event to an outbox table in the same DB transaction, then relay it to Kafka).
  This is intentionally out of scope for this learning project but is the
  natural next step.
- **Simplified status flow.** For simplicity, the `orders.created` event itself
  triggers the move to `PAYMENT_CONFIRMED`. In a real system, payment
  confirmation would be a distinct event published by a separate payment
  process, consumed from its own topic.
- **Single broker / single partition.** The local setup runs one Kafka broker
  with a single-partition topic (replication factor 1), appropriate for
  development but not for production availability.

## License

This project is for educational and portfolio purposes.