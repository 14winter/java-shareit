package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Collection<User> findAll();
    User create(User user);
    User update(User user);
    Optional<User> getUser(Long id);
    void deleteUser(Long id);
}
