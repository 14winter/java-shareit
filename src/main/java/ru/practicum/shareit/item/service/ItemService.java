package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;

import java.util.Collection;

public interface ItemService {
    Collection<ItemDtoBooking> findAllByOwner(Long userId, int from, int size);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    ItemDtoBooking getItem(Long userId, Long id);

    Collection<ItemDto> search(Long userId, String text, int from, int size);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);
}
