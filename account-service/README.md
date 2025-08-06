# Account Service

This project is a Spring Boot application designed to manage bank accounts. It is built with Java 21, Maven, and
PostgreSQL, and is fully containerized using Docker.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

* Java 21: The project is built using JDK 21.
* Maven: Used for dependency management and running build tasks.
* Docker & Docker Compose: Required for building and running the application and its database in containers.

## Getting Started

### 1. How to Launch the Application

The application and its PostgreSQL database are defined in the `compose.yaml` file and can be launched with a single
command. This is the recommended way to run the project for development or testing.

1. **Build and Start the Services**: Open a terminal in the project's root directory and run:
   ```shell
   docker compose up --build -d
   ```
    * `--build`: This flag tells Docker Compose to build the application image from the Dockerfile before starting the
      service.
    * `-d`: This runs the containers in detached mode (in the background).

2. **Verify the Application is Running**: Once the containers are up, the Account Service API will be available at
   `http://localhost:8081`. You can access the OpenAPI documentation at:
    * **Swagger UI**: `http://localhost:8081/swagger-ui/index.html`

3. **Stopping the Application**: To stop and remove the containers, run:
   ```shell
   docker compose down
   ```

### 2. How to Execute the Tests

The project is configured with a full suite of unit and integration tests. To run all tests using Maven, execute the
following command:

```shell 
  mvn test
```

This command will compile the code, run all tests, and generate a test execution report in the `target/surefire-reports`
directory.

### 3. How to Generate and View Code Quality & Coverage Reports

The project is configured with several Maven plugins to enforce code quality and measure test coverage.

#### PMD (Code Analysis)

PMD is a source code analyzer that finds common programming flaws.

1. **Generate the Report**:
   ```shell
   mvn pmd:pmd
   ```
2. **View the Report**: Open the following file in your web browser:
   `target/site/pmd.html`

#### Checkstyle (Code Style)

Checkstyle ensures the code adheres to the Google Java Style Guide.

1. **Generate the Report**:
   ```shell
   mvn checkstyle:checkstyle
   ```
2. **View the Report**: Open the following file in your web browser:
   `target/site/checkstyle.html`

#### Jacoco (Test Coverage)

Jacoco is used to measure code coverage of the tests.

1. **Generate the Report**: The Jacoco report is generated as part of the `test` phase.
   ```shell
   mvn clean test
   ```
2. **View the Report**: Open the following file in your web browser:
   `target/site/jacoco/index.html`

## API Endpoints

The service exposes the following RESTful endpoints for managing account data:

| Method | Path                  | Description              | Roles Allowed |
| ------ |-----------------------|--------------------------| ------------- |
| POST   | /api/v1/account       | Creates a new account.   | ADMIN         |
| GET    | /api/v1/account/{id}  | Retrieves a account.     | ADMIN, USER   |
| GET    | /api/v1/account       | Retrieves all account.   | ADMIN, USER   |
| PUT    | /api/v1/account/{id}  | Updates a account.       | ADMIN         |
| DELETE | /api/v1/account/{id}  | Deletes a account.       | ADMIN         |

## Event-Driven Architecture

This service publishes events to a RabbitMQ topic exchange named `account.events.topic`. This allows for decoupled
communication with other services in the system.

### Published Events

The following events are published:

| Routing Key                | Event Description             |
| ---------------------------|-------------------------------|
| `account.event.created`    | A new account is created.     |
| `account.event.updated`    | A account is updated.         |
| `account.event.deleted`    | A account is deleted.         |

### Consumer Responsibility

It is the responsibility of the consumer services (e.g., `customer-service`) to declare their own queues and bind them
to the `account.events.topic` exchange with the appropriate routing keys. This ensures a decoupled architecture where
the producer is not responsible for the consumer's configuration.
