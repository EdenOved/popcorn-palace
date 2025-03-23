package com.att.tdp.popcorn_palace.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.MovieRepository;

@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {
    private static final Logger log = LoggerFactory.getLogger(ShowtimeController.class);
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    public ShowtimeController(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    // Fetch all showtimes from the database
    @GetMapping
    public ResponseEntity<List<Showtime>> getAllShowtimes() {
        log.info("Fetching all showtimes from database");
        List<Showtime> showtimes = showtimeRepository.findAll();
        if (showtimes.isEmpty()) {
            log.warn("No showtimes found in the database");
            return ResponseEntity.noContent().build();
        }
        log.info("Retrieved {} showtimes", showtimes.size());
        return ResponseEntity.ok(showtimes);
    }

    // Fetch a single showtime by ID
    @GetMapping("/{showtimeId}")
    public ResponseEntity<?> getShowtimeById(@PathVariable Long showtimeId) {
        try {
            return showtimeRepository.findById(showtimeId)
                    .map(showtime -> ResponseEntity.ok().body((Object) showtime))
                    .orElseGet(() -> {
                        log.warn("Showtime ID {} not found", showtimeId);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Error: Showtime ID " + showtimeId + " not found!");
                    });
        } catch (Exception e) {
            log.error("Error fetching showtime ID {}: {}", showtimeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while fetching the showtime.");
        }
    }

    // Fetch all showtimes for a specific movie by movie ID
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Showtime>> getShowtimesByMovie(@PathVariable Long movieId) {
        log.info("Fetching showtimes for movie ID: {}", movieId);
        List<Showtime> showtimes = showtimeRepository.findByMovieId(movieId);
        if (showtimes.isEmpty()) {
            log.warn("No showtimes found for movie ID {}", movieId);
            return ResponseEntity.noContent().build();
        }
        log.info("Retrieved {} showtimes for movie ID {}", showtimes.size(), movieId);
        return ResponseEntity.ok(showtimes);
    }

    // Create a new showtime for a given movie
    @Transactional
    @PostMapping
    public ResponseEntity<?> addShowtime(@RequestBody Showtime showtime) {
        log.info("Attempting to create new showtime for movie ID: {}",
                showtime.getMovie() != null ? showtime.getMovie().getId() : "null");

        try {
            // Ensure movie ID is provided
            if (showtime.getMovie() == null || showtime.getMovie().getId() == null) {
                log.warn("Failed to create showtime - missing movie ID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Movie ID is required!");
            }

            // Verify movie exists in DB
            Optional<Movie> movieOptional = movieRepository.findById(showtime.getMovie().getId());
            if (movieOptional.isEmpty()) {
                log.warn("Movie ID {} not found, cannot create showtime", showtime.getMovie().getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Movie does not exist!");
            }

            // Validate overlapping constraint for same theater
            List<Showtime> existingShowtimes = showtimeRepository.findByTheater(showtime.getTheater());
            for (Showtime existing : existingShowtimes) {
                if (showtimesOverlap(existing, showtime)) {
                    log.warn("Failed to create showtime - overlap detected in theater {}", showtime.getTheater());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Error: Showtime overlaps with existing showtime at " + showtime.getTheater());
                }
            }
            showtime.setMovie(movieOptional.get());
            Showtime savedShowtime = showtimeRepository.save(showtime);
            log.info("Showtime created successfully - ID: {}, Movie ID: {}, Theater: {}",
                    savedShowtime.getId(), savedShowtime.getMovie().getId(), savedShowtime.getTheater());
            return ResponseEntity.ok(savedShowtime);

        } catch (Exception e) {
            log.error("Unexpected error while creating showtime: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while adding the showtime.");
        }
    }

    // Utility method to check for time conflicts in the same theater
    private boolean showtimesOverlap(Showtime existing, Showtime newShowtime) {
        return !newShowtime.getEndTime().isBefore(existing.getStartTime()) &&
                !newShowtime.getStartTime().isAfter(existing.getEndTime());
    }

    // Update an existing showtime
    @Transactional
    @PostMapping("/update/{showtimeId}")
    public ResponseEntity<?> updateShowtime(@PathVariable Long showtimeId, @RequestBody Showtime updatedShowtime) {
        log.info("Attempting to update showtime ID: {}", showtimeId);
        try {
            Optional<Showtime> existingShowtimeOptional = showtimeRepository.findById(showtimeId);
            if (existingShowtimeOptional.isEmpty()) {
                log.warn("Update failed - Showtime ID {} not found", showtimeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Showtime ID not found!");
            }

            // Validate that movie exists before update
            Optional<Movie> movieOptional = movieRepository.findById(updatedShowtime.getMovie().getId());
            if (movieOptional.isEmpty()) {
                log.warn("Update failed - Movie ID {} not found", updatedShowtime.getMovie().getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Movie does not exist!");
            }

            Showtime existingShowtime = existingShowtimeOptional.get();
            existingShowtime.setMovie(movieOptional.get());
            existingShowtime.setTheater(updatedShowtime.getTheater());
            existingShowtime.setPrice(updatedShowtime.getPrice());
            existingShowtime.setStartTime(updatedShowtime.getStartTime());
            existingShowtime.setEndTime(updatedShowtime.getEndTime());

            Showtime savedShowtime = showtimeRepository.save(existingShowtime);
            log.info("Showtime ID {} updated successfully", savedShowtime.getId());
            return ResponseEntity.ok(savedShowtime);

        } catch (Exception e) {
            log.error("Unexpected error while updating showtime '{}': {}", showtimeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while updating the showtime.");
        }
    }

    // Delete a showtime by ID
    @Transactional
    @DeleteMapping("/{showtimeId}")
    public ResponseEntity<?> deleteShowtime(@PathVariable Long showtimeId) {
        log.info("Attempting to delete showtime ID: {}", showtimeId);

        try {
            if (!showtimeRepository.existsById(showtimeId)) {
                log.warn("Delete failed - Showtime ID {} not found", showtimeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Showtime ID not found!");
            }
            showtimeRepository.deleteById(showtimeId);
            log.info("Showtime ID {} deleted successfully", showtimeId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting showtime ID {}: {}", showtimeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while deleting the showtime.");
        }
    }

}
