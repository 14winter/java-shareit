package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ValidationException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final ItemRequestService service;
    private final UserService userService;
    private final ItemService itemService;
    private final User user1 = new User(1L, "Test1", "test1@email.com");
    private final User user2 = new User(2L, "Test2", "test2@email.com");
    private final ItemRequest itemRequest1 = new ItemRequest();
    private final ItemRequest itemRequest2 = new ItemRequest();
    private final Item item = new Item(1L, "New item", "Item for test", true, null, null);

    @BeforeEach
    public void restartIdentity() {
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE;").executeUpdate();
        em.createNativeQuery("TRUNCATE table items restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table users restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table requests restart identity;").executeUpdate();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE;").executeUpdate();
        itemRequest1.setDescription("Test request 1");
        itemRequest2.setDescription("Test request 2");
    }

    @Test
    public void createRequestTest() {
        User user = new User();
        user.setName("Test");
        user.setEmail("test@email.com");
        UserDto userDto = UserMapper.toUserDto(user);
        User createdUser = UserMapper.toUser(userService.create(userDto));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Test description");
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest, Collections.emptyList());
        service.create(1L, itemRequestDto);
        TypedQuery<ItemRequest> query = em.createQuery(
                "select i from ItemRequest i where i.description = :description",
                ItemRequest.class);
        ItemRequest newItemRequest = query.setParameter("description", itemRequest.getDescription()).getSingleResult();
        assertThat(newItemRequest.getId(), notNullValue());
        assertThat(newItemRequest.getId(), equalTo(1L));
        assertThat(newItemRequest.getRequestor().getId(), equalTo(createdUser.getId()));
        assertThat(newItemRequest.getCreated(), notNullValue());
        assertThat(newItemRequest.getDescription(), equalTo(itemRequest.getDescription()));
    }

    @Test
    public void findAllByOwnerTest() {
        item.setRequest(itemRequest1);
        userService.create(UserMapper.toUserDto(user1));
        userService.create(UserMapper.toUserDto(user2));
        ItemRequestDto itemRequestDto1 = service.create(1L, ItemRequestMapper.toItemRequestDto(itemRequest1, Collections.emptyList()));
        ItemRequestDto itemRequestDto2 = service.create(1L, ItemRequestMapper.toItemRequestDto(itemRequest2, Collections.emptyList()));
        itemService.create(2L, ItemMapper.toItemDto(item));
        Collection<ItemRequestDto> requests = service.findAllByOwner(user1.getId());
        List<ItemRequestDto> expectedRequests = List.of(itemRequestDto1, itemRequestDto2);
        expectedRequests.forEach(s -> {
            if (s.getId().equals(item.getRequest().getId())) {
                s.setItems(Set.of(ItemMapper.toItemDto(item)));
            }
        });
        assertThat(requests.size(), equalTo(2));
        assertThat(requests, equalTo(expectedRequests));
    }

    @Test
    public void findAllRequestTest() {
        item.setRequest(itemRequest1);
        userService.create(UserMapper.toUserDto(user1));
        userService.create(UserMapper.toUserDto(user2));
        ItemRequestDto itemRequestDto1 = service.create(1L, ItemRequestMapper.toItemRequestDto(itemRequest1, Collections.emptyList()));
        ItemRequestDto itemRequestDto2 = service.create(1L, ItemRequestMapper.toItemRequestDto(itemRequest2, Collections.emptyList()));
        itemService.create(2L, ItemMapper.toItemDto(item));
        Collection<ItemRequestDto> requests = service.findAll(2L, 0, 5);
        List<ItemRequestDto> expectedRequests = List.of(itemRequestDto1, itemRequestDto2);
        expectedRequests.forEach(s -> {
            if (s.getId().equals(item.getRequest().getId())) {
                s.setItems(List.of(ItemMapper.toItemDto(item)));
            }
        });
        assertThat(requests.size(), equalTo(2));
        assertThat(requests, equalTo(expectedRequests));
    }

    @Test
    public void getRequestByIdTest() {
        userService.create(UserMapper.toUserDto(user1));
        userService.create(UserMapper.toUserDto(user2));
        ItemRequestDto itemRequestDto = service.create(1L, ItemRequestMapper.toItemRequestDto(itemRequest1, Collections.emptyList()));
        item.setRequest(ItemRequestMapper.toItemRequest(itemRequestDto));
        itemService.create(2L, ItemMapper.toItemDto(item));
        ItemRequestDto newItemRequest1 = service.getRequest(1L, 1L);
        assertThat(newItemRequest1.getId(), equalTo(1L));
        assertThat(newItemRequest1.getItems(), equalTo(List.of(ItemMapper.toItemDto(item))));
        assertThat(newItemRequest1.getCreated(), equalTo(itemRequestDto.getCreated()));
        assertThat(newItemRequest1.getDescription(), equalTo("Test request 1"));
    }

    @Test
    void findAll_InvalidFrom() {
        userService.create(UserMapper.toUserDto(user1));

        assertThrows(ValidationException.class, () -> service.findAll(1L, -1, 5));
    }

    @Test
    void findAll_InvalidSize() {
        userService.create(UserMapper.toUserDto(user1));

        assertThrows(ValidationException.class, () -> service.findAll(1L, 0, -5));
    }

    @Test
    void findAll_InvalidUserId() {

        assertThrows(UserNotFoundException.class, () -> service.findAll(1000L, 0, -5));
    }
}