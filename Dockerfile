# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app/backend
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Set default environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

COPY --from=builder /app/backend/target/graduation-project-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]