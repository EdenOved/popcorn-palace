package com.att.tdp.popcorn_palace;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.att.tdp.popcorn_palace.controller.MovieController;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

@WebMvcTest(MovieController.class)
public class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieRepository movieRepository;

    @MockBean
    private ShowtimeRepository showtimeRepository;

    @Test
    void testGetAllMovies_returnsOk() throws Exception {
        List<Movie> mockMovies = List.of(new Movie(1L, "Matrix", "Action", 120, 8.5, 1999));
        when(movieRepository.findAll()).thenReturn(mockMovies);

        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Matrix"));
    }

    @Test
    void testGetAllMovies_returnsNoContent() throws Exception {
        when(movieRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isNoContent());
    }
}
