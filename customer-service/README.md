# Customer Service

This project is a Spring Boot application designed to manage bank customers information. It is built with Java 21,
Maven, and PostgreSQL, and is fully containerized using Docker.

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

2. **Verify the Application is Running**: Once the containers are up, the Customer Service API will be available at
   `http://localhost:8080`. You can access the OpenAPI documentation at:
    * **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

3. **Using the APIs**: To use the APIs, you have to provide basic auth, two users are saved in-memory as a prof of
   concept:

   | User  | Password | Role(s)        |
   |-------|----------|----------------|
   | user  | user     | USER           |
   | admin | admin    | ADMIN<br/>USER |

4. Feel free to use the Postman collection for exploring the APIs:
    * [Postman Collection](Customer%20Service.postman_collection.json)

5. **Stopping the Application**: To stop and remove the containers, run:
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
   `target/reports/pmd.html`

#### Checkstyle (Code Style)

Checkstyle ensures the code adheres to the Google Java Style Guide.

1. **Generate the Report**:
   ```shell
   mvn checkstyle:checkstyle
   ```
2. **View the Report**: Open the following file in your web browser:
   `target/checkstyle-result.xml`

#### Jacoco (Test Coverage)

Jacoco is used to measure code coverage of the tests.

1. **Generate the Report**: The Jacoco report is generated as part of the `test` phase.
   ```shell
   mvn clean test
   ```
2. **View the Report**: Open the following file in your web browser:
   `target/site/jacoco/index.html`

## API Endpoints

The service exposes the following RESTful endpoints for managing customer data:

| Method | Path                  | Description              | Roles Allowed |
|--------|-----------------------|--------------------------|---------------|
| POST   | /api/v1/customer      | Creates a new customer.  | ADMIN         |
| GET    | /api/v1/customer/{id} | Retrieves a customer.    | ADMIN, USER   |
| GET    | /api/v1/customer      | Retrieves all customers. | ADMIN, USER   |
| PUT    | /api/v1/customer/{id} | Updates a customer.      | ADMIN         |
| DELETE | /api/v1/customer/{id} | Deletes a customer.      | ADMIN         |

## Event-Driven Architecture

This service publishes events to a RabbitMQ topic exchange named `customer.events.topic`. This allows for decoupled
communication with other services in the system.

### Published Events

The following events are published:

| Routing Key              | Event Description          |
|--------------------------|----------------------------|
| `customer.event.created` | A new customer is created. |
| `customer.event.updated` | A customer is updated.     |
| `customer.event.deleted` | A customer is deleted.     |

### Consumer Responsibility

It is the responsibility of the consumer services (e.g., `account-service`) to declare their own queues and bind them to
the `customer.events.topic` exchange with the appropriate routing keys. This ensures a decoupled architecture where the
producer is not responsible for the consumer's configuration.
