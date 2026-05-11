# Unified Reservation Orchestration System (UROS)

A reusable backend reservation orchestration engine built with **Spring Boot 3** and **MySQL**, supporting multiple reservation domains through a unified API.

## Supported Reservation Types

| Type | Use Case | Example |
|------|----------|---------|
| **TIME_BASED** | Slot-based bookings with parallel capacity | Salon, SPA, appointments |
| **RESOURCE_BASED** | Date-range bookings with conflict detection | Rooms, banquet halls |
| **SEAT_BASED** | Specific seat selection from a seat map | Events, transport (RedBus) |
| **QUOTA_BASED** | Allocation from predefined quota pools | Train tickets, priority access |
| **CAPACITY_BASED** | Aggregate capacity without individual units | General admission, bulk booking |

## Architecture

```
Client / Third-Party
        │
   API Gateway (REST)
        │
┌───────┴────────────────────────────────────┐
│           Reservation Controller           │
│                    │                       │
│           Reservation Service              │
│                    │                       │
│         Reservation Orchestrator           │
│          ┌────┬────┼────┬────┐             │
│          │    │    │    │    │             │
│        Time Res  Seat Quota Cap           │
│        Based Based Based Based Based       │
│       Strategy Implementations             │
│                    │                       │
│              Policy Engine                 │
│                    │                       │
│         Expiry Scheduler                   │
└────────────────────┬───────────────────────┘
                     │
              MySQL Database
```

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.x (running on `localhost:3306`)

## Quick Start

### 1. Clone and configure database

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS reservation_orchestration_db;"
```

### 2. Update database credentials

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: root
    password: root  # change to your MySQL password
```

### 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Access

- **Application:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs:** http://localhost:8081/v3/api-docs
- **Health:** http://localhost:8081/actuator/health

## API Endpoints

### Resource Types
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/resource-types` | Create resource type |
| GET | `/api/v1/resource-types` | List all types |
| GET | `/api/v1/resource-types/{id}` | Get by ID |
| PUT | `/api/v1/resource-types/{id}` | Update |
| DELETE | `/api/v1/resource-types/{id}` | Delete |

### Resources
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/resources` | Register resource |
| GET | `/api/v1/resources` | List resources |
| GET | `/api/v1/resources/{id}` | Get by ID |
| PUT | `/api/v1/resources/{id}` | Update |
| DELETE | `/api/v1/resources/{id}` | Delete |

### Time Slots
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/time-slots` | Create time slot |
| GET | `/api/v1/time-slots/resource/{id}` | Get slots for resource |

### Seat Maps
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/seat-maps` | Create seat |
| GET | `/api/v1/seat-maps/resource/{id}` | All seats |
| GET | `/api/v1/seat-maps/resource/{id}/available` | Available seats |

### Quotas
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/quotas` | Create quota |
| GET | `/api/v1/quotas/resource/{id}` | Get quotas |

### Reservations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/reservations/availability` | Check availability |
| POST | `/api/v1/reservations` | Create reservation (hold) |
| POST | `/api/v1/reservations/{id}/confirm` | Confirm booking |
| POST | `/api/v1/reservations/{id}/cancel` | Cancel |
| GET | `/api/v1/reservations/{id}` | Get details |
| GET | `/api/v1/reservations?userId=X` | List by user |

### Policies
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/policies` | Create policy |
| GET | `/api/v1/policies` | List all |
| PUT | `/api/v1/policies/{id}` | Update |
| DELETE | `/api/v1/policies/{id}` | Delete |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/dashboard/stats` | Reservation statistics |
| GET | `/api/v1/dashboard/utilization` | Resource utilization |

## Reservation Lifecycle

```
[INITIATED] → [HELD] → [CONFIRMED]
                 │            │
                 ↓            ↓
             [EXPIRED]   [CANCELLED]
```

## Key Design Patterns

- **Strategy Pattern** — Each reservation type has its own strategy implementation
- **Orchestrator** — Central coordinator that routes to the correct strategy
- **Pessimistic Locking** — Prevents double-booking on high-contention resources
- **Optimistic Locking** — `@Version` field on reservations for concurrent updates
- **Scheduled Expiry** — Background job to expire unconfirmed holds

## Running Tests

```bash
mvn test
mvn test -Dtest=ConcurrencyTest
```

## Project Structure

```
src/main/java/com/uros/
├── UrosApplication.java
├── config/              # Swagger configuration
├── common/              # Shared enums, DTOs, exceptions, base entity
├── resource/            # Resource registration module
├── reservation/         # Reservation management module
├── engine/              # Core reservation engine
│   ├── strategy/        # Strategy pattern implementations
│   ├── orchestrator/    # Central orchestrator
│   └── scheduler/       # Expiry scheduler
├── policy/              # Administration & policy module
└── dashboard/           # Monitoring & analytics module
```

## License

Academic / Dissertation Project
