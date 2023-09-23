package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestAddDto itemRequestAddDto);

    Collection<ItemRequestDto> findAllByOwner(Long userId);

    Collection<ItemRequestDto> findAll(Long userId, int from, int size);

    ItemRequestDto getRequest(Long userId, Long requestId);
}
