package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    Collection<User> findAll();

    User create(User user);

    User update(User user, Long id);

    User getUser(Long id);

    void deleteUser(Long id);
}
