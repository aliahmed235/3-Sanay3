# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src
COPY backend/.mvn ./backend/.mvn
COPY backend/mvnw ./backend/
WORKDIR /app/backend
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/backend/target/graduation-project-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]