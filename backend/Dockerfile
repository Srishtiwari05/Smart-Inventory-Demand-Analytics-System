# Stage 1: Build the Spring Boot application using Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies first for efficient layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Java 17 Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-privileged user for secure execution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled jar from build stage
COPY --from=builder /app/target/smart-inventory-system-1.0-SNAPSHOT.jar app.jar

# Set container ownership
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/products || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
