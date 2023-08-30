package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
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
        User user = getUserById(id);
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && !userDto.getEmail().equals(user.getEmail())) {
            if (!userRepository.isEmailUnique(userDto.getEmail())) {
                throw new UserAlreadyExistException("Пользователь с таким email уже существует");
            }
            user.setEmail(userDto.getEmail());
        }
        userRepository.update(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUser(Long id) {
        log.info("Получен запрос на получение пользователя");
        User user = getUserById(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Получен запрос на удаление пользователя");
        getUserById(id);
        userRepository.deleteUser(id);
    }

    private User getUserById(Long id) {
        if (id <= 0) {
            log.info("id {} должен быть больше ноля", id);
            throw new IllegalArgumentException("id должен быть больше ноля");
        }
        return userRepository.getUser(id)
                .orElseThrow(() -> {
                    log.info("Пользователь с id {} не найден", id);
                    throw new UserNotFoundException(id);
                });
    }
}
