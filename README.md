# Sky E-commerce Kafka Consumer

Spring Boot service for e-commerce events with:
- Authentication (signup, login, OTP verification)
- Order API and domain events
- Kafka listeners for domain/generic events
- Outbox pattern (entity, repository, service, scheduler)
- Monitoring endpoints and in-memory message store
- Event envelope abstraction

## Tech Stack
- Java 17+
- Spring Boot
- Apache Kafka
- Maven
- Spring Data JPA
- Spring Security

## Structure
- com/sky_ecommerce/auth: API, domain, service, security
- com/sky_ecommerce/order: API, domain, service, events
- com/sky_ecommerce/outbox: outbox entity, repository, scheduler, service
- com/sky_ecommerce/listeners: Kafka listeners
- com/sky_ecommerce/common: EventEnvelope and factory
- com/sky_ecommerce/config: Kafka configuration
- com/sky_ecommerce/monitor: monitoring controller and store
- com/example/kafkaconsumer: example listeners and main application
- src/main/resources/application.yml: configuration

## Prerequisites
- JDK 17+
- Maven 3.9+
- Kafka broker (local or remote)
- Database if persistence is enabled (configure in application.yml)

## Configuration
Update src/main/resources/application.yml:
- spring.kafka.bootstrap-servers
- consumer group-id/client-id (if used)
- topic names
- security settings for auth
- datasource settings if not embedded

Ensure required Kafka topics exist (or enable auto-create on broker).

## Build and Run
Build:
- mvn clean package

Run (dev):
- mvn spring-boot:run

Run jar:
- java -jar target/kafka-consumer-1.0.0.jar

## REST Endpoints (high level)
Auth:
- POST /api/auth/signup
- POST /api/auth/verify-otp
- POST /api/auth/login

Orders:
- POST /api/orders (see CreateOrderRequest)
- Other endpoints as defined in OrderController

Events:
- POST /api/events/publish (PublishController)

Monitoring:
- GET /api/monitor/messages

## Kafka Consumption
- EcommerceListeners for domain events
- Example listeners: EventListener, OrderListener
- Events use EventEnvelope via EventEnvelopeFactory

## Outbox
- Persist events in OutboxEntity
- OutboxScheduler publishes pending records to Kafka and marks them sent

## Development
- Adjust application.yml to your environment
- Review SecurityConfig for access control
- For DB-backed outbox, ensure schema or enable DDL auto

## Notes
