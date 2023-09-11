package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {
    BookingDto create(Long userId, BookingAddDto bookingAddDto);

    BookingDto update(Long userId, Long bookingId, String approved);

    BookingDto getBooking(Long userId, Long bookingId);

    Collection<BookingDto> findAllByUser(Long userId, BookingState state);

    Collection<BookingDto> findAllByOwner(Long ownerId, BookingState state);
}
