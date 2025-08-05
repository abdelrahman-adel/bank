# Stage 1: Build the application
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# Copy the Maven wrapper and make sure the Unix script is executable
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/

# Pre‑fetch dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the rest of your code
COPY src ./src

# Build the application, skipping tests
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final, smaller image
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/target/account-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java","-jar","app.jar"]
