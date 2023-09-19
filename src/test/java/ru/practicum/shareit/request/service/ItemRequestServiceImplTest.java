package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
public class ItemRequestServiceImplTest {
    @Autowired
    ItemRequestService itemRequestService;
    @Autowired
    UserService userService;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDto createdItemRequestDto;
    private UserDto userDto;

    @BeforeEach
    void beforeEach() {
        User user = User.builder().id(1L).name("user").email("user@email.com").build();
        userDto = UserMapper.toUserDto(user);
        userService.create(userDto);
        LocalDateTime created = LocalDateTime.parse("2022-10-05T01:00");
        ItemRequest itemRequest = new ItemRequest(1L, "description", user, created);
        itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest, Collections.emptyList());
    }

    @Test
    void createTest() {
        createdItemRequestDto = itemRequestService.create(userDto.getId(), itemRequestDto);
        assertThat(createdItemRequestDto.getId()).isNotNull();
        assertEquals(createdItemRequestDto.getId(), 1);
        assertEquals(createdItemRequestDto.getDescription(), itemRequestDto.getDescription());
        assertEquals(createdItemRequestDto, itemRequestService.getRequest(userDto.getId(), itemRequestDto.getId()));
    }

    @Test
    void findAllByOwnerTest() {
        createdItemRequestDto = itemRequestService.create(userDto.getId(), itemRequestDto);
        Collection<ItemRequestDto> requests = itemRequestService.findAllByOwner(userDto.getId());
        assertThat(requests).hasSize(1).contains(createdItemRequestDto);
    }

    @Test
    void findAllTest() {
        createdItemRequestDto = itemRequestService.create(userDto.getId(), itemRequestDto);
        Collection<ItemRequestDto> requests = itemRequestService.findAll(userDto.getId(), 0, 2);
        assertThat(requests).hasSize(0);
    }

    @Test
    void getRequestTest() {
        createdItemRequestDto = itemRequestService.create(userDto.getId(), itemRequestDto);
        assertThat(itemRequestService.getRequest(userDto.getId(), createdItemRequestDto.getId()).getId()).isNotZero();
        assertEquals(createdItemRequestDto.getDescription(), itemRequestService.getRequest(userDto.getId(), createdItemRequestDto.getId()).getDescription());
        assertThat(itemRequestService.getRequest(userDto.getId(), createdItemRequestDto.getId()).getItems().size()).isZero();
    }
}
