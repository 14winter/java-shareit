package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    BookingDto bookingDto;
    BookingAddDto bookingAddDto;
    ItemDto itemDto;
    UserDto userDto;
    Long userId = 1L;
    Long bookingId = 1L;
    LocalDateTime start = LocalDateTime.now().plusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(3);

    @BeforeEach
    void beforeEach() {
        userDto = UserDto.builder().id(1L).name("user").email("user@email.com").build();
        itemDto = ItemDto.builder().id(1L).name("name").description("description").available(true).build();
        bookingDto = BookingDto.builder().id(1L).start(start).end(end).item(itemDto)
                .booker(userDto).status(BookingStatus.WAITING).build();
        bookingAddDto = BookingAddDto.builder().start(start).end(end).itemId(1L).build();
    }

    @Test
    public void createBooking() throws Exception {
        when(bookingService.create(any(), any()))
                .thenReturn(bookingDto);

        mockMvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .header("X-Sharer-User-Id", userId)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.item.id").value(bookingDto.getItem().getId()));

        verify(bookingService, Mockito.times(1)).create(any(), any());
    }

    @Test
    public void createBookingWithUserNotFoundException() throws Exception {
        when(bookingService.create(any(), any()))
                .thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .header("X-Sharer-User-Id", userId)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingService, Mockito.times(1)).create(any(), any());
    }

    @Test
    public void approvedBooking() throws Exception {
        when(bookingService.update(userId, bookingId, "true"))
                .thenReturn(bookingDto);
        mockMvc.perform(
                        patch("/bookings/{bookingId}", bookingId)
                                .header("X-Sharer-User-Id", userId)
                                .param("approved", "true")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(1)));

        verify(bookingService, Mockito.times(1)).update(userId, bookingId, "true");
    }

    @Test
    public void approvedBookingWithDataNotFoundException() throws Exception {
        when(bookingService.update(userId, bookingId, "true"))
                .thenThrow(new DataNotFoundException(bookingId));
        mockMvc.perform(
                        patch("/bookings/{bookingId}", bookingId)
                                .header("X-Sharer-User-Id", userId)
                                .param("approved", "true")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, Mockito.times(1)).update(userId, bookingId, "true");
    }

    @Test
    public void getBooking() throws Exception {
        when(bookingService.getBooking(userId, bookingId))
                .thenReturn(bookingDto);
        mockMvc.perform(
                        get("/bookings/{bookingId}", bookingId)
                                .header("X-Sharer-User-Id", userId)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(1)));
        verify(bookingService, Mockito.times(1)).getBooking(userId, bookingId);
    }

    @Test
    public void getBookingUserNotFoundException() throws Exception {
        when(bookingService.getBooking(userId, bookingId))
                .thenThrow(new UserNotFoundException(userId));
        mockMvc.perform(
                        get("/bookings/{bookingId}", bookingId)
                                .header("X-Sharer-User-Id", userId)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, Mockito.times(1)).getBooking(userId, bookingId);
    }

    @Test
    public void getBookingBookingNotFoundException() throws Exception {
        when(bookingService.getBooking(userId, bookingId))
                .thenThrow(new BookingNotFoundException(""));
        mockMvc.perform(
                        get("/bookings/{bookingId}", bookingId)
                                .header("X-Sharer-User-Id", userId)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, Mockito.times(1)).getBooking(userId, bookingId);
    }

    @Test
    public void findAllByUser() throws Exception {
        when(bookingService.findAllByUser(userId, BookingState.ALL, 0, 10))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(
                        get("/bookings")
                                .header("X-Sharer-User-Id", userId)
                                .param("state", "ALL")
                                .param("from", String.valueOf(0))
                                .param("size", String.valueOf(10))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(1)));
        verify(bookingService, Mockito.times(1)).findAllByUser(userId, BookingState.ALL, 0, 10);
    }

    @Test
    public void findAllByUserNotParameter() throws Exception {
        when(bookingService.findAllByUser(userId, BookingState.ALL, 0, 10))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(
                        get("/bookings")
                                .header("X-Sharer-User-Id", userId)
                                .param("state", "ALL")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(1)));
        verify(bookingService, Mockito.times(1)).findAllByUser(userId, BookingState.ALL, 0, 10);
    }

    @Test
    public void findAllByOwner() throws Exception {
        when(bookingService.findAllByOwner(userId, BookingState.ALL, 0, 10))
                .thenReturn(List.of(bookingDto));
        mockMvc.perform(
                        get("/bookings/owner")
                                .header("X-Sharer-User-Id", userId)
                                .param("state", "ALL")
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].item.id", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(1)));
        verify(bookingService, Mockito.times(1)).findAllByOwner(userId, BookingState.ALL, 0, 10);
    }
}
