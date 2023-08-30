package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Collection<ItemDto> findAllByOwner(Long userId) {
        log.info("Получен запрос на получение списка вещей пользователя по id {}", userId);
        User user = getUserById(userId);
        return itemRepository.findAllByOwner(user).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Получен запрос на создание вещи");
        Item createdItem = itemRepository.create(userId, ItemMapper.toItem(itemDto));
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        log.info("Получен запрос на обновление вещи");
        getUserById(userId);
        Item item = getItemById(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            log.info("У пользователя по id {} нет вещи с id {}", userId, itemId);
            throw new DataNotFoundException(itemId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if ((itemDto.getAvailable() != null)) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemRepository.update(userId, item));
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        log.info("Получен запрос на получение вещи");
        getUserById(userId);
        Item item = getItemById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        log.info("Получен запрос на поиск вещей от пользователя по id {}", userId);
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private User getUserById(Long userId) {
        if (userId <= 0) {
            log.info("id {} должен быть больше ноля", userId);
            throw new IllegalArgumentException("id должен быть больше ноля");
        }
        return userRepository.getUser(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь с id {} не найден", userId);
                    return new UserNotFoundException(userId);
                });
    }

    private Item getItemById(Long itemId) {
        if (itemId <= 0) {
            log.info("id {} должен быть больше ноля", itemId);
            throw new IllegalArgumentException("id должен быть больше ноля");
        }
        return itemRepository.getItem(itemId)
                .orElseThrow(() -> {
                    log.info("Вещь с id {} не найдена", itemId);
                    return new DataNotFoundException(itemId);
                });
    }
}
