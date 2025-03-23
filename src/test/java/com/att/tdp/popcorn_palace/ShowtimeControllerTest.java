package com.att.tdp.popcorn_palace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.att.tdp.popcorn_palace.controller.ShowtimeController;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

@WebMvcTest(ShowtimeController.class)
public class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeRepository showtimeRepository;

    @MockBean
    private MovieRepository movieRepository;

    @Test
    void testGetAllShowtimes_returnsOk() throws Exception {
        Movie movie = new Movie(1L, "Matrix", "Action", 120, 8.5, 1999);
        Showtime showtime = new Showtime(1L, movie, "Theater 1", 30.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        when(showtimeRepository.findAll()).thenReturn(List.of(showtime));

        mockMvc.perform(get("/showtimes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].theater").value("Theater 1"));
    }

    @Test
    void testGetShowtimesByMovie_returnsNoContent() throws Exception {
        Long movieId = 999L;
        when(showtimeRepository.findByMovieId(movieId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/showtimes/movie/" + movieId))
                .andExpect(status().isNoContent());
    }
}
