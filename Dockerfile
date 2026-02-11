FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 10000

# Simple direct approach - let Spring Boot handle the PORT variable
ENV PORT=10000
ENV SERVER_PORT=10000
CMD ["java", "-Dspring.profiles.active=render", "-jar", "app.jar"]