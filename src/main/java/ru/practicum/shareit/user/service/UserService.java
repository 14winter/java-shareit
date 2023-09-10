package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> findAll();

    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto, Long id);

    UserDto getUser(Long id);

    void deleteUser(Long id);
}
