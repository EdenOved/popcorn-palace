# Popcorn Palace - Movie Ticket Booking System

A Spring Boot RESTful API for managing movies, showtimes, and seat bookings.  
The system supports full CRUD operations for movies and showtimes, as well as secure and validated seat reservations.

---

## Features

### Movies
- Add new movies with title, genre, duration, rating, and release year.
- Update existing movie details.
- Delete movies (only if no showtimes exist).
- Fetch all movies or a movie by ID.

### Showtimes
- Create showtimes for specific movies, including theater, time range, and price.
- Prevent overlapping showtimes in the same theater.
- Update or delete showtimes.
- Fetch showtime by ID or by movie.

### Bookings
- Book seats for a specific showtime.
- Prevent double-booking of the same seat.
- Update or delete existing bookings.
- Fetch booking by ID or by showtime.

---

## Example APIs

### Movies

| Method | Endpoint                           | Description                 |
|--------|------------------------------------|-----------------------------|
| GET    | `/movies/all`                      | Get all movies              |
| GET    | `/movies/{id}`                     | Get a movie by ID           |
| POST   | `/movies`                          | Add a new movie             |
| POST   | `/movies/update/{movieTitle}`      | Update a movie by title     |
| DELETE | `/movies/{movieTitle}`             | Delete a movie by title     |


### Showtimes

| Method | Endpoint                           | Description                 |
|--------|------------------------------------|-----------------------------|
| GET    | `/showtimes`                       | Get all showtimes           |
| GET    | `/showtimes/{showtimeId}`          | Get showtime by ID          |
| GET    | `/showtimes/movie/{movieId}`       | Get showtimes by movie ID   |
| POST   | `/showtimes`                       | Add new showtime            |
| POST   | `/showtimes/update/{showtimeId}`   | Update showtime by ID       |
| DELETE | `/showtimes/{showtimeId}`          | Delete showtime by ID       |

### Bookings

| Method | Endpoint                          | Description                  |
|--------|-----------------------------------|------------------------------|
| GET    | `/bookings/{id}`                  | Get booking by ID            |
| GET    | `/bookings/showtime/{showtimeId}` | Get bookings by showtime ID  |
| POST   | `/bookings`                       | Create new booking           |
| POST   | `/bookings/update/{id}`           | Update booking by ID         |
| DELETE | `/bookings/{id}`                  | Delete booking by ID         |


## Example JSON Requests

#### Add New Movie

```http
POST /movies
Content-Type: application/json
```

```json
{
  "title": "Inception",
  "genre": "Sci-Fi",
  "duration": 148,
  "rating": 8.8,
  "releaseYear": 2010
}
```

---

#### Add New Showtime

```http
POST /showtimes
Content-Type: application/json
```

```json
{
  "movieId": 1,
  "theater": "The Grand Hall",
  "price": 45.00,
  "startTime": "2025-03-25T18:30:00",
  "endTime": "2025-03-25T20:30:00"
}
```

---

#### Create New Booking

```http
POST /bookings
Content-Type: application/json
```

```json
{
  "showtimeId": 1,
  "seatNumber": 12,
  "userId": "123e4567-e89b-12d3-a456-426614174000"
}
```

---

## Tech Stack

- Java 17  
- Spring Boot  
- PostgreSQL  
- JPA (Hibernate)  
- JUnit 5 + MockMvc for testing

---

## Getting Started

The app connects to a PostgreSQL database with the following credentials:

```
Database: popcorn-palace  
Username: popcorn-palace  
Password: popcorn-palace  
Port: 5432
```

You can either run PostgreSQL locally, or use the included `compose.yml` file with Docker.

---

## Project Structure

- `controller/` - REST controllers for each entity  
- `model/` - JPA entities  
- `repository/` - Spring Data interfaces  
- `test/` - Unit tests with MockMvc

---

## Testing

Run all tests using:

```bash
mvn test
```

Tests are written to cover the most critical flows in each controller.

---

## Notes

- The database schema is created automatically at runtime (no need for `schema.sql` or `data.sql`)
- API validations and error handling are implemented (e.g. for duplicates or invalid input).
- This README summarizes the implemented endpoints and behaviors.
- This project was submitted as part of the ATT backend assignment.
  
