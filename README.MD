# The Asset Management Digital Challenge

This project is based on a challenge for a basic banking system with operations for account creation, account inquiry, and money transfer between accounts.

The solution meets the requirements specified in the challenge (challenge.pdf) and is designed to be scalable and easily maintainable.

## Functionality

The application is built with the Spring Boot framework and uses an in-memory database to store accounts and their balances.

To run the application, Java and Gradle need to be installed, and the following command can be used:

`./gradlew bootRun`

The application exposes a REST API with the following endpoints:

- POST /accounts: creates a new bank account.
- GET /accounts/{id}: retrieves the balance of a bank account.
- POST /transfers: performs a transfer between two bank accounts.

## Testing

The application has a set of unit and integration tests that can be executed with the following command:

`./gradlew test`

Additionally, manual tests can be performed using a tool like Postman (Collections included in the 'postman' folder).

This suite provide testing to the following scenarios:

- Transfer from a non-existent account: the application should return a specific error message indicating that the account does not exist.
- Transfer from an account with insufficient funds: the application should return a specific error message indicating that there is not enough balance in the account.
- Transfer with an amount equal to zero: the application should return a specific error message indicating that the amount must be greater than zero.
- Transfer with a negative amount: the application should return a specific error message indicating that the amount must be greater than zero.
- Transfer between two accounts that are the same: the application should return a specific error message indicating that the source account cannot be the same as the destination account.
- Successful transfer: it should be verified that the balances of the two accounts have been correctly updated in the repository (by invoking the GET method).

## Improvements

The current project meets the requirements of the challenge and is designed to be easily maintainable and scalable. However, there are some improvements that could be implemented to make it even better:

- Use real persistence and transactions (Spring Data, Transactional, Flyway, Liquibase).
- Document everything properly (Javadoc API).
- Document REST API (AsyncAPI, Swagger) with generators using an API-FIRST approach.
- Implement end-to-end integration testing (Selenium, REST Assured...).
- Implement a messaging system (Kafka, RabbitMQ) to communicate between different microservices (Events).
- Implement security measures (Spring Security, JWT, Authentication, Authorization...).
- Use reactive technology to improve efficiency in microservices (Spring WebFlux, R2DBC...).
- Implement hexagonal architecture / DDD (folders: domain, infrastructure, domain... applying vertical slicing).
- Implement CQRS to separate read and write operations.
- Implement microservices architecture patterns (API Gateway, Service Discovery, Circuit Breaker).
- Implement tracing and metrics (SLF4J, ELK, Sleuth, Grafana, Prometheus, Actuator...).
- Implement containerization and orchestration (Docker, Kubernetes, Openshift, etc.).
- Implement load testing with JMeter.
- Implement test coverage tools (JaCoCo).
- Implement gRPC-Protobuf.
- Private cloud BYOC
