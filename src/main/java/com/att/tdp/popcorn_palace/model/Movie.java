package com.att.tdp.popcorn_palace.model;

import jakarta.persistence.*;
import lombok.*;

// Marks this class as a JPA entity to be mapped to the "movies" table in the database
// Represents a movie with its details (title, genre, duration, rating, release year)
@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The movie title must be unique – we identify movies by title
    @Column(name = "title", unique = true, nullable = false)
    private String title;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private int duration;

    private double rating;

    @Column(nullable = false)
    private int releaseYear;

}
