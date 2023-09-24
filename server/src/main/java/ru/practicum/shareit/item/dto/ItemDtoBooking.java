package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;

import java.util.Collection;

@Data
@Builder
public class ItemDtoBooking {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long request;
    private BookingDtoForItem lastBooking;
    private BookingDtoForItem nextBooking;
    private Collection<CommentDto> comments;
}
