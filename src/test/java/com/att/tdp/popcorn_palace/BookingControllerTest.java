package com.att.tdp.popcorn_palace;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.att.tdp.popcorn_palace.controller.BookingController;
import com.att.tdp.popcorn_palace.model.Booking;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateBooking_successful() throws Exception {
        Showtime showtime = new Showtime();
        showtime.setId(1L);

        Booking booking = new Booking(null, showtime, "user1", 5);
        Booking savedBooking = new Booking(1L, showtime, "user1", 5);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.findByShowtimeId(1L)).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seatNumber").value(5));
    }

    @Test
    void testCreateBooking_conflict_seatTaken() throws Exception {
        Showtime showtime = new Showtime();
        showtime.setId(1L);

        Booking existingBooking = new Booking(1L, showtime, "user1", 5);
        Booking newBooking = new Booking(null, showtime, "user2", 5);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(bookingRepository.findByShowtimeId(1L)).thenReturn(Collections.singletonList(existingBooking));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBooking)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Error: Seat is already taken!"));
    }
}
