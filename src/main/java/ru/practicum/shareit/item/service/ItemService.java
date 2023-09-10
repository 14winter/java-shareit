package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    Collection<ItemDto> findAllByOwner(Long userId);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    ItemDto getItem(Long userId, Long id);

    Collection<ItemDto> search(Long userId, String text);
}
