# User Service

This project is a Spring Boot application designed to manage user information and handle file uploads associated with user requests. It is built with Java 21, Maven, and PostgreSQL, and is fully containerized using Docker.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

*   Java 21: The project is built using JDK 21.
*   Maven: Used for dependency management and running build tasks.
*   Docker & Docker Compose: Required for building and running the application and its database in containers.

## Getting Started

### 1. How to Launch the Application

The application and its PostgreSQL database are defined in the `compose.yaml` file and can be launched with a single command. This is the recommended way to run the project for development or testing.

1.  **Build and Start the Services**: Open a terminal in the project's root directory and run:
    ```shell
    docker compose up --build -d
    ```
    *   `--build`: This flag tells Docker Compose to build the application image from the Dockerfile before starting the service.
    *   `-d`: This runs the containers in detached mode (in the background).

2.  **Verify the Application is Running**: Once the containers are up, the User Service API will be available at `http://localhost:8080`. You can access the OpenAPI documentation at:
    *   **Swagger UI**: `http://localhost:8080/swagger-ui.html`

3.  **Stopping the Application**: To stop and remove the containers, run:
    ```shell
    docker compose down
    ```

### 2. How to Execute the Tests

The project is configured with a full suite of unit and integration tests. To run all tests using Maven, execute the following command:
```shell 
  mvn test
```

This command will compile the code, run all tests, and generate a test execution report in the `target/surefire-reports` directory.

### 3. How to Generate and View Code Quality & Coverage Reports

The project is configured with several Maven plugins to enforce code quality and measure test coverage.

#### PMD (Code Analysis)

PMD is a source code analyzer that finds common programming flaws.

1.  **Generate the Report**:
    ```shell
    mvn pmd:pmd
    ```
2.  **View the Report**: Open the following file in your web browser:
    `target/site/pmd.html`

#### Checkstyle (Code Style)

Checkstyle ensures the code adheres to the Google Java Style Guide.

1.  **Generate the Report**:
    ```shell
    mvn checkstyle:checkstyle
    ```
2.  **View the Report**: Open the following file in your web browser:
    `target/site/checkstyle.html`

#### Jacoco (Test Coverage)

Jacoco is used to measure code coverage of the tests.

1.  **Generate the Report**: The Jacoco report is generated as part of the `test` phase.
    ```shell
    mvn clean test
    ```
2.  **View the Report**: Open the following file in your web browser:
    `target/site/jacoco/index.html`

## Event-Driven Architecture

This service publishes a message to the `customer.created.account` queue in RabbitMQ when a new user is created. The `account-service` listens to this queue and creates a default account for the new user.