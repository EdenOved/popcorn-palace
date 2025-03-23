package com.att.tdp.popcorn_palace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.att.tdp.popcorn_palace.model.Showtime;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByTheater(String theater);
}
