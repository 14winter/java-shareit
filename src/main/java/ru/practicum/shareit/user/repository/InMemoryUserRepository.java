package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @Override
    public Collection<User> findAll() {
        log.info("Текущее количество пользователей: {}", users.size());
        return users.values();
    }

    @Override
    public User create(User user) {
        if (!isEmailUnique(user.getEmail())) {
            throw new UserAlreadyExistException("Пользователь с таким email уже существует");
        }
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: {}", user);
        return user;

    }

    @Override
    public Optional<User> getUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            log.info("Пользователь с id {} не найден", id);
            throw new UserNotFoundException(id);
        }
        return Optional.of(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = users.get(id);
        if (user == null) {
            log.info("Пользователь с id {} не найден", id);
            throw new UserNotFoundException(id);
        }
        users.remove(user.getId());
        log.info("Пользователь с id {} удален", id);
    }

    private Long generateId() {
        return id++;
    }

    @Override
    public boolean isEmailUnique(String email) {
        return users.values().stream()
                .noneMatch(user -> user.getEmail().equals(email));
    }
}
