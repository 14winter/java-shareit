package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem() != null ? booking.getItem() : null)
                .booker(booking.getBooker() != null ? booking.getBooker() : null)
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingAddDto bookingAddDto) {
        return Booking.builder()
                .start(bookingAddDto.getStart())
                .end(bookingAddDto.getEnd())
                .build();
    }

    public static BookingDtoForItem toBookingDtoForItem(Booking booking) {
        return BookingDtoForItem.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}
