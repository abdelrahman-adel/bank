# Bank Project

This project contains two microservices: `account-service` and `customer-service`.

## How to Launch

Both services can be launched together using Docker Compose.

1.  **Build and Start the Services**: Open a terminal in the project's root directory and run:
    ```shell
    docker compose up --build -d
    ```
    * `--build`: This flag tells Docker Compose to build the application images from the Dockerfiles before starting the services.
    * `-d`: This runs the containers in detached mode (in the background).

2.  **Verify the Applications are Running**:
    *   **Customer Service**: `http://localhost:8080`
    *   **Account Service**: `http://localhost:8081`

3.  **Stopping the Application**: To stop and remove the containers, run:
    ```shell
    docker compose down
    ```

## Service-Specific Documentation

For more detailed information about each service, please refer to their individual `README.md` files and Postman collections:

* Account Service:
    * [README](account-service/README.md)
    * [Postman Collection](account-service/Account%20Service.postman_collection.json)
* Customer Service:
    * [README](customer-service/README.md)
    * [Postman Collection](customer-service/Customer%20Service.postman_collection.json)
