package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestAddDto itemRequestAddDto) {
        log.info("Получен запрос новой вещи пользователем с id {}", userId);
        User user = getUserById(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequestItemRequestAddDto(itemRequestAddDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        ItemRequest createdItemRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(createdItemRequest, Collections.emptyList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemRequestDto> findAllByOwner(Long userId) {
        log.info("Получен запрос на получение списка запросов вещей пользователя с id {}", userId);
        getUserById(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId);

        Map<Long, ItemDto> itemsMap = itemRepository.findAllByRequest_IdIn(itemRequests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(item -> item.getRequest().getId(), ItemMapper::toItemDto));

        return itemRequests.stream()
                .map(ItemRequest -> ItemRequestMapper.toItemRequestDto(ItemRequest, itemsMap.containsKey(ItemRequest.getId())
                        ? List.of(itemsMap.get(ItemRequest.getId())) : List.of()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> findAll(Long userId, int from, int size) {
        log.info("Получен запрос на получение списка всех запросов вещей от пользователя с id {}", userId);
        getUserById(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequestor_IdNot(userId, pageRequest);

        Map<Long, ItemDto> itemsMap = itemRepository.findAllByRequest_IdIn(itemRequests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(item -> item.getRequest().getId(), ItemMapper::toItemDto));

        return itemRequests.stream()
                .map(ItemRequest -> ItemRequestMapper.toItemRequestDto(ItemRequest, itemsMap.containsKey(ItemRequest.getId())
                        ? List.of(itemsMap.get(ItemRequest.getId())) : List.of()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getRequest(Long userId, Long requestId) {
        log.info("Получен запрос вещи с id {} от пользователя с id {}", requestId, userId);
        getUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        List<ItemDto> items = itemRepository.findAllByRequest_id(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return ItemRequestMapper.toItemRequestDto(itemRequest, items);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь с id {} не найден", userId);
                    return new UserNotFoundException(userId);
                });
    }
}
