# CargoFlow

A logistics management REST/GraphQL API built as a portfolio project to demonstrate production-ready backend development practices with Java and Spring Boot.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| API | Spring GraphQL |
| Database | PostgreSQL + jOOQ (no JPA/Hibernate) |
| Migrations | Liquibase |
| Search | Elasticsearch 9 (full-text autocomplete) |
| Security | Spring Security + JWT (stateless) |
| Infrastructure | Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers |

> **Why jOOQ instead of JPA?** jOOQ gives full control over SQL — no N+1 problems, no magic, explicit queries. Every JOIN is intentional.

---

## Features

- **Auth** — register and login with JWT token response
- **Role hierarchy** — `ADMIN` → `MANAGER` → `SHIPPER`, enforced via `@PreAuthorize` on every resolver
- **Shipment lifecycle** — create, track by number, assign carrier, update status, cancel
- **Address autocomplete** — Elasticsearch `search_as_you_type` field with full-text search
- **Event indexing** — addresses indexed to ES after commit via `@TransactionalEventListener`
- **Pagination** — `getAllShipments(page, size)` with jOOQ `LIMIT/OFFSET`
- **Global error handling** — 404, 409, 401, 500 mapped to GraphQL errors

---

## Architecture

```
graphql/        — resolvers (@QueryMapping / @MutationMapping)
service/        — business logic, @Transactional
repository/     — jOOQ queries, manual mapping, LEFT JOINs
domain/         — plain entities (no JPA annotations)
dto/            — GraphQL input types
security/       — JWT filter, SecurityConfig, JwtService
exception/      — NotFoundException, AlreadyExistsException
elasticsearch/  — AddressDocument, AddressSearchRepository, AddressIndexListener
```

Each layer has a single responsibility. Repositories only do SQL. Services own transactions and domain logic. Resolvers only delegate.

---

## Getting Started

**Prerequisites:** Docker Desktop, Java 21, Maven

```bash
# 1. Clone the repository
git clone https://github.com/qqrayzqq/cargoflow_practice.git
cd cargoflow_practice

# 2. Start infrastructure (PostgreSQL + Elasticsearch)
docker compose up -d

# 3. Run the application (Liquibase migrations run automatically)
./mvnw spring-boot:run
```

GraphQL playground available at: `http://localhost:8080/graphiql`

---

## GraphQL Examples

### Register and get JWT token
```graphql
mutation {
  register(input: {
    username: "john_doe"
    email: "john@example.com"
    password: "secret123"
    fullName: "John Doe"
  })
}
```

### Create a shipment
```graphql
mutation {
  createShipment(input: {
    fromAddress: { country: "CZ", zip: "11000", city: "Prague", street: "Wenceslas Square", buildingNumber: "1" }
    toAddress:   { country: "DE", zip: "10115", city: "Berlin", street: "Unter den Linden", buildingNumber: "5" }
    parcels: [{ weight: 2.5, width: 30.0, height: 20.0, length: 40.0, isFragile: false }]
  }) {
    id
    trackingNumber
    status
  }
}
```

### Track a shipment (no auth required)
```graphql
query {
  getShipmentByTrackingNumber(trackingNumber: "TRK-XXXXXXXX") {
    trackingNumber
    status
    fromAddress { city }
    toAddress { city }
    events { status occurredAt }
  }
}
```

### Address autocomplete (Elasticsearch)
```graphql
query {
  searchAddresses(query: "Pra") {
    id
    city
    street
  }
}
```

### Assign carrier (MANAGER role required)
```graphql
mutation {
  assignCarrier(id: 1, carrierId: 2) {
    id
    carrier { name }
    status
  }
}
```

---

## Security Model

All endpoints require a `Bearer <token>` header except:
- `register` / `login` mutations
- `getShipmentByTrackingNumber` query

| Role | Access |
|---|---|
| `SHIPPER` | Create shipments, view own shipments, cancel own shipments |
| `MANAGER` | Everything above + view all shipments, assign carriers, update status |
| `ADMIN` | Full access (inherits MANAGER via role hierarchy) |

---

## Testing

Three layers of tests covering different concerns:

```
Unit tests (Mockito)
  ShipmentServiceTest   — 15 tests, business logic in full isolation
  ShipmentResolverTest  — 4 tests, resolver delegation to service

Integration tests (Testcontainers + real PostgreSQL)
  UserRepositoryTest    — 3 tests, jOOQ queries against real DB schema
```

```bash
./mvnw test
```

---

## Database Schema

Managed by Liquibase (8 migrations). Core tables:

```
users → shipments → parcels
              ↓
         addresses (from / to)
              ↓
       shipment_events
              ↓
          carriers
```

`carrier_id` is nullable — a shipment can exist without an assigned carrier.

---

## What I Learned Building This

- Writing complex SQL with jOOQ manually (multi-table LEFT JOINs, manual record mapping)
- Designing a stateless JWT security chain from scratch
- Indexing data to Elasticsearch reliably using transactional event listeners
- Structuring a GraphQL API with role-based access control
- Writing tests at multiple layers: unit, integration, and resolver level
