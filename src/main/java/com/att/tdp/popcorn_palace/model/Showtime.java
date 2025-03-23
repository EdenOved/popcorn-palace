package com.att.tdp.popcorn_palace.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

// Marks this class as a JPA entity to be mapped to the "showtimes" table in the database
// Represents a single showtime, including time, price, theater and linked movie
@Entity
@Table(name = "showtimes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many showtimes can be linked to the same movie (Many-to-One relationship)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private String theater;

    private Double price;

    // Start time of the show (date + hour)
    @Column(nullable = false)
    private LocalDateTime startTime;

    // End time of the show (used to prevent overlapping)
    @Column(nullable = false)
    private LocalDateTime endTime;
}
