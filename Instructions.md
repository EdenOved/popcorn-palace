# Popcorn Palace - Instructions 

This is a Spring Boot-based REST API for managing movies, showtimes, and ticket bookings.

## Prerequisites

Make sure you have the following installed before running the project:

- Java 17 or higher  
- Maven 3.8 or higher  
- PostgreSQL running locally or via Docker

The application expects a PostgreSQL database named `popcorn-palace` running on port `5432`,  
with user/password: `popcorn-palace`.  
You may use Docker or run PostgreSQL locally.

## Build the Project

To build the application and run tests:

```bash
mvn clean install
```

This will compile the project, run unit tests, and create the final build.

## Run the Application

To start the Spring Boot application locally:

```bash
mvn spring-boot:run
```

The app will start on:

```
http://localhost:8080
```

You can now access and test the API using Postman or any other REST client.

## Run Tests

To run all unit tests:

```bash
mvn test
```

You’ll see the test results in the terminal.  
The tests cover key functionality for each controller.

## Notes

- The app uses a real **PostgreSQL** database — schema is auto-created via JPA.
- No sample data is inserted — the database starts empty.
- API validation and error handling are implemented as required.
- The API structure and behavior follow the instructions in `README.md`.
