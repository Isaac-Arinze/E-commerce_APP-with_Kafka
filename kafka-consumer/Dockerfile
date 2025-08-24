# Use Maven to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -f kafka-consumer/pom.xml clean package -DskipTests

# Use a lightweight JRE for running the app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/kafka-consumer/target/kafka-consumer-0.0.1-SNAPSHOT.jar app.jar
# Expose the default Spring Boot port
EXPOSE 8080
# Use environment variables for Java options if needed
ENTRYPOINT ["java","-jar","app.jar"]
