package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(properties = "db.name=test", webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRequestService itemRequestService;
    private UserDto user1;
    private UserDto user2;
    private ItemRequestDto request;
    ItemDto itemDto = new ItemDto();
    CommentDto commentDto = new CommentDto();

    private final BookingAddDto lastBookingDto = new BookingAddDto(LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), 1L);
    private final BookingAddDto nextBookingDto = new BookingAddDto(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(5), 1L);

    @BeforeEach
    public void restart() {
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE;").executeUpdate();
        em.createNativeQuery("TRUNCATE table items restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table users restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table bookings restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table requests restart identity;").executeUpdate();
        em.createNativeQuery("TRUNCATE table comments restart identity;").executeUpdate();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE;").executeUpdate();
        user1 = userService.create(UserDto.builder().name("User1").email("user1@test.ru").build());
        user2 = userService.create(UserDto.builder().name("User2").email("user2@test.ru").build());
        request = itemRequestService.create(user2.getId(), ItemRequestDto.builder().description("Request item").build());
        itemDto.setRequestId(1L);
        itemDto.setAvailable(true);
        itemDto.setDescription("Description item");
        itemDto.setName("New item");
    }

    @Test
    public void createItemTest() {
        ItemDto item = itemService.create(user2.getId(), itemDto);
        assertThat(item.getId(), equalTo(1L));
        assertThat(item.getRequestId(), equalTo(request.getId()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getAvailable(), equalTo(true));
    }

    @Test
    public void updateItemTest() {
        ItemDto item = itemService.create(user1.getId(), itemDto);
        ItemDto updateItem = ItemDto.builder().name("Update name").description("Description after update").available(false).requestId(1L).build();
        ItemDto itemAfterUpdate = itemService.update(user1.getId(), updateItem, item.getId());
        assertThat(itemAfterUpdate.getId(), equalTo(1L));
        assertThat(itemAfterUpdate.getRequestId(), equalTo(request.getId()));
        assertThat(itemAfterUpdate.getDescription(), equalTo(updateItem.getDescription()));
        assertThat(itemAfterUpdate.getName(), equalTo(updateItem.getName()));
        assertThat(itemAfterUpdate.getAvailable(), equalTo(false));
    }

    @Test
    public void getItemTest() {
        ItemDto item = itemService.create(user1.getId(), itemDto);
        BookingDto lastBooking = bookingService.create(user2.getId(), lastBookingDto);
        BookingDto nextBooking = bookingService.create(user2.getId(), nextBookingDto);
        bookingService.update(user1.getId(), lastBooking.getId(), "true");
        bookingService.update(user1.getId(), nextBooking.getId(), "true");
        commentDto.setText("Comment for item1");
        CommentDto comment = itemService.createComment(user2.getId(), item.getId(), commentDto);
        ItemDtoBooking itemDtoBooking = itemService.getItem(item.getId(), user1.getId());
        assertThat(itemDtoBooking.getId(), equalTo(1L));
        assertThat(itemDtoBooking.getName(), equalTo(item.getName()));
        assertThat(itemDtoBooking.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDtoBooking.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDtoBooking.getRequest(), equalTo(request.getId()));
        assertThat(itemDtoBooking.getLastBooking(), equalTo(BookingMapper.toToBookingDtoForItemBookingDto(lastBooking)));
        assertThat(itemDtoBooking.getNextBooking(), equalTo(BookingMapper.toToBookingDtoForItemBookingDto(nextBooking)));
        assertThat(itemDtoBooking.getComments(), equalTo(List.of(comment)));
    }

    @Test
    public void findAllByOwnerItemsTest() {
        ItemDto item1 = itemService.create(user1.getId(), itemDto);
        BookingDto lastBooking = bookingService.create(user2.getId(), lastBookingDto);
        bookingService.update(user1.getId(), lastBooking.getId(), "true");
        ItemDto item2 = itemService.create(user1.getId(), ItemDto.builder().name("item2").description("Description item2").available(false).build());
        commentDto.setText("Comment for item1");
        CommentDto comment = itemService.createComment(user2.getId(), item1.getId(), commentDto);
        List<ItemDtoBooking> itemDtoBookingList = List.copyOf(itemService.findAllByOwner(user1.getId(), 0, 5));
        assertThat(itemDtoBookingList.size(), equalTo(2));
        assertThat(itemDtoBookingList.get(0).getName(), equalTo(item1.getName()));
        assertThat(itemDtoBookingList.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(itemDtoBookingList.get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(itemDtoBookingList.get(0).getRequest(), equalTo(request.getId()));
        assertThat(itemDtoBookingList.get(0).getLastBooking(), equalTo(BookingMapper.toToBookingDtoForItemBookingDto(lastBooking)));
        assertThat(itemDtoBookingList.get(0).getNextBooking(), nullValue());
        assertThat(itemDtoBookingList.get(0).getComments(), equalTo(List.of(comment)));
        assertThat(itemDtoBookingList.get(1).getName(), equalTo(item2.getName()));
        assertThat(itemDtoBookingList.get(1).getDescription(), equalTo(item2.getDescription()));
        assertThat(itemDtoBookingList.get(1).getAvailable(), equalTo(item2.getAvailable()));
        assertThat(itemDtoBookingList.get(1).getRequest(), equalTo(null));
        assertThat(itemDtoBookingList.get(1).getLastBooking(), nullValue());
        assertThat(itemDtoBookingList.get(1).getNextBooking(), nullValue());
        assertThat(itemDtoBookingList.get(1).getComments(), equalTo(new ArrayList<>()));
    }

    @Test
    public void searchItemsTest() {
        ItemDto item = itemService.create(user1.getId(), itemDto);
        String text = "new item";
        List<ItemDto> itemList = List.copyOf(itemService.search(user1.getId(), text, 0, 5));
        assertThat(itemList.size(), equalTo(1));
        assertThat(itemList.get(0), equalTo(item));
    }

    @Test
    public void createCommentTest() {
        ItemDto item1 = itemService.create(user1.getId(), itemDto);
        bookingService.create(user2.getId(), lastBookingDto);
        commentDto.setText("Comment for item1");
        CommentDto comment = itemService.createComment(user2.getId(), item1.getId(), commentDto);
        assertThat(comment.getId(), equalTo(1L));
        assertThat(comment.getCreated(), notNullValue());
        assertThat(comment.getAuthorName(), equalTo(user2.getName()));
        assertThat(comment.getText(), equalTo(commentDto.getText()));
    }

    @Test
    void findAllByOwner_InvalidFrom() {

        assertThrows(ValidationException.class, () -> itemService.findAllByOwner(1L, -1, 5));
    }

    @Test
    void findAllByOwner_InvalidSize() {

        assertThrows(ValidationException.class, () -> itemService.findAllByOwner(1L, 0, -5));
    }

    @Test
    void search_InvalidFrom() {

        assertThrows(ValidationException.class, () -> itemService.findAllByOwner(1L, -1, 5));
    }

    @Test
    void search_InvalidSize() {

        assertThrows(ValidationException.class, () -> itemService.findAllByOwner(1L, 0, -5));
    }
}