package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceImplTest {
    Long ID = 1L;
    private UserServiceImpl userServiceImpl;
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void beforeEach() {
        userRepository = mock(UserRepository.class);
        userServiceImpl = new UserServiceImpl(userRepository);
        user = new User(ID, "user", "user@email.com");
    }

    @Test
    void createUserTest() {
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        UserDto savedUserDto = UserMapper.toUserDto(user);
        UserDto userDto = userServiceImpl.create(savedUserDto);

        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(userDto.getId(), ID);
        Assertions.assertEquals(userDto.getName(), savedUserDto.getName());
    }

    @Test
    void updateUserTest() {
        user.setName("updated name");
        UserDto savedUserDto = UserMapper.toUserDto(user);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(user));

        UserDto userDto = userServiceImpl.update(savedUserDto, ID);

        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(userDto.getId(), 1);
        Assertions.assertEquals(userDto.getName(), savedUserDto.getName());
    }

    @Test
    void findUserByIdTest() {
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(user));

        UserDto userDto = userServiceImpl.getUser(ID);

        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(1, userDto.getId());
    }

    @Test
    void findAllUsersTest() {
        when(userRepository.findAll())
                .thenReturn(Collections.singletonList(user));

        Collection<UserDto> userDtoList = userServiceImpl.findAll();

        Assertions.assertNotNull(userDtoList);
        Assertions.assertEquals(1, userDtoList.size());
        Assertions.assertEquals(user.getId(), userDtoList.stream().findFirst().get().getId());

    }

    @Test
    void deleteUserByIdTest() {
        userServiceImpl.deleteUser(ID);
        verify(userRepository, times(1)).deleteById(ID);
    }
}