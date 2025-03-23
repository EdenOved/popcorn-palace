package com.att.tdp.popcorn_palace.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;

    public BookingController(BookingRepository bookingRepository, ShowtimeRepository showtimeRepository) {
        this.bookingRepository = bookingRepository;
        this.showtimeRepository = showtimeRepository;
    }

    // Fetch booking by its ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking by ID: {}", id);

        return bookingRepository.findById(id)
                .map(booking -> {
                    log.info("Booking found: ID {}", id);
                    return ResponseEntity.ok().body((Object) booking);
                })
                .orElseGet(() -> {
                    log.warn("Booking ID {} not found", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(("Error: Booking ID " + id + " not found!"));
                });
    }

    // Fetch bookings for a specific showtime
    @GetMapping("/showtime/{showtimeId}")
    public ResponseEntity<?> getBookingsByShowtime(@PathVariable Long showtimeId) {
        log.info("Fetching bookings for showtime ID: {}", showtimeId);
        List<Booking> bookings = bookingRepository.findByShowtimeId(showtimeId);
        if (bookings.isEmpty()) {
            log.warn("No bookings found for showtime ID {}", showtimeId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Error: No bookings for showtime ID " + showtimeId);
        }
        log.info("Retrieved {} bookings for showtime ID {}", bookings.size(), showtimeId);
        return ResponseEntity.ok(bookings);
    }

    // Create a new booking with seat validation
    @Transactional
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        log.info("Attempting to create new booking for showtime ID: {}, seat number: {}",
                booking.getShowtime() != null ? booking.getShowtime().getId() : "null", booking.getSeatNumber());

        try {
            // Validate that showtime ID exists
            if (booking.getShowtime() == null || booking.getShowtime().getId() == null) {
                log.warn("Failed to create booking - missing showtime ID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Showtime ID is required!");
            }

            Optional<Showtime> showtimeOptional = showtimeRepository.findById(booking.getShowtime().getId());
            if (showtimeOptional.isEmpty()) {
                log.warn("Showtime ID {} not found, cannot create booking", booking.getShowtime().getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Showtime does not exist!");
            }

            // Prevent duplicate booking for the same seat
            List<Booking> existingBookings = bookingRepository.findByShowtimeId(booking.getShowtime().getId());
            for (Booking existingBooking : existingBookings) {
                if (existingBooking.getSeatNumber() == booking.getSeatNumber()) {
                    log.warn("Failed to create booking - seat {} already taken for showtime ID {}",
                            booking.getSeatNumber(), booking.getShowtime().getId());
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Seat is already taken!");
                }
            }

            booking.setShowtime(showtimeOptional.get());
            Booking savedBooking = bookingRepository.save(booking);
            log.info("Booking created successfully - ID: {}, Showtime ID: {}, Seat: {}",
                    savedBooking.getId(), savedBooking.getShowtime().getId(), savedBooking.getSeatNumber());
            return ResponseEntity.ok(savedBooking);

        } catch (Exception e) {
            log.error("Unexpected error while creating booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while creating the booking.");
        }
    }

    // Update an existing booking by ID with seat validation
    @Transactional
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Booking updatedBooking) {
        log.info("Attempting to update booking ID: {}", id);

        try {
            Optional<Booking> existingBookingOptional = bookingRepository.findById(id);
            if (existingBookingOptional.isEmpty()) {
                log.warn("Update failed - Booking ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Booking ID not found!");
            }

            // Validate that showtime exists
            Optional<Showtime> showtimeOptional = showtimeRepository.findById(updatedBooking.getShowtime().getId());
            if (showtimeOptional.isEmpty()) {
                log.warn("Update failed - Showtime ID {} not found", updatedBooking.getShowtime().getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Showtime does not exist!");
            }

            // Prevent assigning an already-booked seat to another booking
            Showtime showtime = showtimeOptional.get();
            List<Booking> existingBookings = bookingRepository.findByShowtimeId(showtime.getId());
            for (Booking booking : existingBookings) {
                if (booking.getSeatNumber() == updatedBooking.getSeatNumber() && !booking.getId().equals(id)) {
                    log.warn("Update failed - Seat {} already taken for Showtime ID {}",
                            updatedBooking.getSeatNumber(), updatedBooking.getShowtime().getId());
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Seat is already taken!");
                }
            }
            Booking existingBooking = existingBookingOptional.get();
            existingBooking.setShowtime(showtime);
            existingBooking.setUserId(updatedBooking.getUserId());
            existingBooking.setSeatNumber(updatedBooking.getSeatNumber());

            Booking savedBooking = bookingRepository.save(existingBooking);
            log.info("Booking ID {} updated successfully", savedBooking.getId());
            return ResponseEntity.ok(savedBooking);

        } catch (Exception e) {
            log.error("Unexpected error while updating booking ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while updating the booking.");
        }
    }

    // Delete a booking by ID
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        log.info("Attempting to delete booking ID: {}", id);

        try {
            if (!bookingRepository.existsById(id)) {
                log.warn("Delete failed - Booking ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Booking ID not found!");
            }

            bookingRepository.deleteById(id);
            log.info("Booking ID {} deleted successfully", id);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Unexpected error while deleting booking ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: An unexpected error occurred while deleting the booking.");
        }
    }

}
