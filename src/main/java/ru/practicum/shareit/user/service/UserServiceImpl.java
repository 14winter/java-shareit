package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> findAll() {
        log.info("Получен запрос на получение списка пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Получен запрос на создание пользователя");
        User createdUser = userRepository.save(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long id) {
        log.info("Получен запрос на обновление пользователя");
        UserDto updatedUser = getUser(id);
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            updatedUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank() && !userDto.getEmail().equals(updatedUser.getEmail())) {
            updatedUser.setEmail(userDto.getEmail());
        }
        userRepository.save(UserMapper.toUser(updatedUser));
        return updatedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(Long id) {
        log.info("Получен запрос на получение пользователя");
        User user = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException(id));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Получен запрос на удаление пользователя");
        userRepository.deleteById(id);
    }
}
