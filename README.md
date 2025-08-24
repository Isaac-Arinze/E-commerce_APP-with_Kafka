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

## Docker

### Build Docker Image
```
docker build -t kafka-consumer .
```

### Run Docker Container
```
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://<host>:<port>/<db> \
  -e DB_USERNAME=<db_user> \
  -e DB_PASSWORD=<db_pass> \
  -e KAFKA_BOOTSTRAP_SERVERS=<kafka_host:port> \
  -e JWT_SECRET=<jwt_secret> \
  -e MAIL_USERNAME=<mail_user> \
  -e MAIL_PASSWORD=<mail_pass> \
  -e CLOUDINARY_CLOUD_NAME=<cloudinary_name> \
  -e CLOUDINARY_API_KEY=<cloudinary_key> \
  -e CLOUDINARY_API_SECRET=<cloudinary_secret> \
  kafka-consumer
```

Set all required environment variables as needed for your deployment.

## Deploying to Render

1. **Create a new Web Service** on Render and connect your GitHub repo.
2. **Build Command:**
   ```
   ./mvnw clean package -DskipTests
   ```
3. **Start Command:**
   ```
   java -jar kafka-consumer/target/kafka-consumer-0.0.1-SNAPSHOT.jar
   ```
4. **Environment Variables:**
   - Set all sensitive and environment-specific values (see Docker run example above).
   - Example:
     - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_SECRET`, etc.
5. **Port:**
   - Set to `8080` (default for Spring Boot)

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
