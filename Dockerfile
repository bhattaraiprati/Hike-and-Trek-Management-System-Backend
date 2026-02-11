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

# Create a startup script
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'exec java -Dspring.profiles.active=render -Dserver.port=${PORT:-10000} -Dserver.address=0.0.0.0 -jar /app/app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

ENTRYPOINT ["/app/start.sh"]