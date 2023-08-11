package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.Optional;

public interface UserService {
    Collection<UserDto> findAll();
    UserDto create(UserDto userDto);
    UserDto update(UserDto userDto);
    UserDto getUser(Long id);
    void deleteUser(Long id);
}
