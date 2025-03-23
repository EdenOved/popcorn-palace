package com.att.tdp.popcorn_palace.model;

import jakarta.persistence.*;
import lombok.*;

// Marks this class as a JPA entity to be mapped to the "bookings" table in the database
// Represents a ticket booking for a specific showtime, by a specific user and seat
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many bookings can be linked to the same showtime (Many-to-One relationship)
    @ManyToOne
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private int seatNumber;
}
