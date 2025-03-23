package com.att.tdp.popcorn_palace.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieRepository movieRepository;
    private static final Logger log = LoggerFactory.getLogger(MovieController.class);
    private final ShowtimeRepository showtimeRepository;

    public MovieController(MovieRepository movieRepository, ShowtimeRepository showtimeRepository) {
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
    }

    // Fetch all movies from the database
    @GetMapping("/all")
    public ResponseEntity<?> getAllMovies() {
        log.info("Fetching all movies from database");
        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            log.warn("No movies found in the database");
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Error: No movies available in the database!");
        }
        log.info("Retrieved {} movies", movies.size());
        return ResponseEntity.ok(movies);
    }

    // Fetch a specific movie by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> getMovieById(@PathVariable Long id) {
        log.info("Fetching movie by ID: {}", id);
        return movieRepository.findById(id)
                .map(movie -> {
                    log.info("Movie found: {}", movie.getTitle());
                    return ResponseEntity.ok().body((Object) movie);
                })
                .orElseGet(() -> {
                    log.warn("Movie ID {} not found", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body((Object) ("Error: Movie ID " + id + " not found!"));
                });
    }

    // Create a new movie
    @Transactional
    @PostMapping
    public ResponseEntity<?> addMovie(@Valid @RequestBody Movie movie) {
        log.info("Creating new movie: {}", movie.getTitle());

        try {
            // Check if movie with same title already exists
            if (movieRepository.findByTitle(movie.getTitle()).isPresent()) {
                log.warn("Movie '{}' already exists in the database", movie.getTitle());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Movie already exists!");
            }
            // Validate that title is not null or empty
            if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
                log.warn("Failed to create movie: title cannot be empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Movie title cannot be empty!");
            }

            Movie savedMovie = movieRepository.save(movie);
            log.info("Movie '{}' added successfully with ID {}", savedMovie.getTitle(), savedMovie.getId());
            return ResponseEntity.ok(savedMovie);

        } catch (Exception e) {
            log.error("Error occurred while creating a new movie: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while adding the movie.");
        }
    }

    // Update an existing movie by its title
    @Transactional
    @PostMapping("/update/{movieTitle}")
    public ResponseEntity<?> updateMovie(@PathVariable String movieTitle, @RequestBody Movie updatedMovie) {
        log.info("Updating movie: {} -> {}", movieTitle, updatedMovie.getTitle());
        try {
            Optional<Movie> existingMovie = movieRepository.findByTitle(movieTitle);
            if (existingMovie.isEmpty()) {
                log.warn("Update failed: Movie '{}' not found", movieTitle);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Movie not found!");
            }

            // Prevent changing to a title that already exists in another movie
            if (!movieTitle.equals(updatedMovie.getTitle())
                    && movieRepository.findByTitle(updatedMovie.getTitle()).isPresent()) {
                log.warn("Update failed: Movie with title '{}' already exists", updatedMovie.getTitle());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Movie with this title already exists!");
            }

            Movie movie = existingMovie.get();
            movie.setTitle(updatedMovie.getTitle());
            movie.setGenre(updatedMovie.getGenre());
            movie.setDuration(updatedMovie.getDuration());
            movie.setRating(updatedMovie.getRating());
            movie.setReleaseYear(updatedMovie.getReleaseYear());

            Movie savedMovie = movieRepository.save(movie);
            log.info("Movie '{}' updated successfully with ID {}", savedMovie.getTitle(), savedMovie.getId());
            return ResponseEntity.ok(savedMovie);

        } catch (Exception e) {
            log.error("Unexpected error while updating movie '{}': {}", movieTitle, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while updating the movie.");
        }
    }

    // Delete a movie by title
    @Transactional
    @DeleteMapping("/{movieTitle}")
    public ResponseEntity<?> deleteMovie(@PathVariable String movieTitle) {
        log.info("Attempting to delete movie: {}", movieTitle);

        try {
            Optional<Movie> movie = movieRepository.findByTitle(movieTitle);
            if (movie.isEmpty()) {
                log.warn("Delete failed: Movie '{}' not found", movieTitle);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Movie not found!");
            }

            // Prevent deletion if movie has existing showtimes
            List<Showtime> showtimes = showtimeRepository.findByMovieId(movie.get().getId());
            if (!showtimes.isEmpty()) {
                log.warn("Delete failed: Movie '{}' has existing showtimes", movieTitle);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: Cannot delete movie with existing showtimes!");
            }

            movieRepository.delete(movie.get());
            log.info("Movie '{}' deleted successfully", movieTitle);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Unexpected error while deleting movie '{}': {}", movieTitle, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while deleting the movie.");
        }
    }

}
