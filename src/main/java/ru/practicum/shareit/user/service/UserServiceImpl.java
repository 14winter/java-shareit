package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Collection<UserDto> findAll() {
        log.info("Получен запрос на получение списка пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Получен запрос на создание пользователя");
        User createdUser = userRepository.create(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(UserDto userDto, Long id) {
        log.info("Получен запрос на обновление пользователя");
        User updatedUser = userRepository.update(UserMapper.toUser(userDto), id);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUser(Long id) {
        log.info("Получен запрос на получение пользователя");
        User user = userRepository.getUser(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Получен запрос на удаление пользователя");
        userRepository.deleteUser(id);
    }
}
