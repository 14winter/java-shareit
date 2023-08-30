package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 1L;
    private final InMemoryUserRepository userRepository;

    @Override
    public Collection<Item> findAllByOwner(User owner) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(owner))
                .collect(Collectors.toList());
    }

    @Override
    public Item create(Long userId, Item item) {
        item.setOwner(userRepository.getUser(userId).orElseThrow(() -> {
            log.info("Пользователь с id {} не найден", userId);
            return new UserNotFoundException(userId);
        }));
        if (item.getAvailable() == null) {
            throw new ValidationException("Нельзя создать вещь без статуса доступа");
        }
        item.setId(generateId());
        items.put(item.getId(), item);
        log.info("Добавлена вещь: {}", item);
        return item;
    }

    @Override
    public Item update(Long userId, Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            log.info("Вещь обновлена: {}", item);
            return item;
        } else {
            log.info("Вещь с id {} не найдена", item.getId());
            throw new DataNotFoundException(item.getId());
        }
    }

    @Override
    public Optional<Item> getItem(Long id) {
        Item item = items.get(id);
        if (item == null) {
            log.info("Вещь с id {} не найдена", id);
            throw new DataNotFoundException(id);
        }
        return Optional.of(item);
    }

    @Override
    public Collection<Item> search(String text) {
        String searchString = text.toLowerCase().trim();

        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(searchString) || item.getDescription().toLowerCase().contains(searchString))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }

    private Long generateId() {
        return id++;
    }
}
