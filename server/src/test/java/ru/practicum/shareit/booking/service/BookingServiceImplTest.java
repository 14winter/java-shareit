package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.item.dto.ItemMapper.toItem;
import static ru.practicum.shareit.item.dto.ItemMapper.toItemDto;
import static ru.practicum.shareit.user.dto.UserMapper.toUser;
import static ru.practicum.shareit.user.dto.UserMapper.toUserDto;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    private User user;

    private Item item;

    @BeforeEach
    public void beforeEach() {
        user = User.builder()
                .id(1L)
                .name("test")
                .email("test@test.ru").build();
        user = toUser(userService.create(toUserDto(user)));
        item = Item.builder()
                .id(1L)
                .owner(user)
                .description("Test")
                .name("Test")
                .available(true).build();
        item = toItem(itemService.create(1L, toItemDto(item)));
        User user1 = User.builder()
                .id(2L)
                .name("test1")
                .email("test1@test.ru").build();
        user1 = toUser(userService.create(toUserDto(user1)));
        Item item1 = Item.builder()
                .id(2L)
                .owner(user1)
                .description("Test1")
                .name("Test1")
                .available(true).build();
        item1 = toItem(itemService.create(2L, toItemDto(item1)));
        Item item2 = Item.builder()
                .id(3L)
                .owner(user)
                .description("Test2")
                .name("Test2")
                .available(false).build();
        item2 = toItem(itemService.create(1L, toItemDto(item2)));
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user1)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.WAITING).build());
        Booking booking1 = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user1)
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .status(BookingStatus.WAITING).build());
        Booking booking2 = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user1)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.WAITING).build());
    }

    @Test
    void createBooking() {
        BookingAddDto bookingAddDto = BookingAddDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now()).build();
        bookingService.create(2L, bookingAddDto);
        List<BookingDto> testBookings1 = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.WAITING, 0, 10));

        assertEquals(2L, testBookings1.get(3).getId());
        assertTrue(testBookings1.get(2).getStart().isAfter(testBookings1.get(3).getStart()));
        assertTrue(testBookings1.get(3).getStart().isBefore(testBookings1.get(1).getStart()));
    }

    @Test
    void createBooking_InvalidUserId() {
        BookingAddDto bookingAddDto = BookingAddDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now()).build();

        assertThrows(UserNotFoundException.class, () -> bookingService.create(100L, bookingAddDto));
    }

    @Test
    void createBooking_InvalidItemId() {
        BookingAddDto bookingAddDto = BookingAddDto.builder()
                .itemId(100L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now()).build();

        assertThrows(DataNotFoundException.class, () -> bookingService.create(user.getId(), bookingAddDto));
    }

    @Test
    void createBooking_OwnerItem() {
        BookingAddDto bookingAddDto = BookingAddDto.builder()
                .itemId(1L).start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2)).build();

        assertThrows(BookingNotFoundException.class, () -> bookingService.create(user.getId(), bookingAddDto));
    }

    @Test
    void createBooking_Unavailable() {
        BookingAddDto bookingAddDto = BookingAddDto.builder()
                .itemId(3L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusHours(2)).build();

        assertThrows(ValidationException.class, () -> bookingService.create(user.getId(), bookingAddDto));
    }

    @Test
    void approvedBooking() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        Collection<BookingDto> testBookings = bookingService.findAllByUser(2L, BookingState.WAITING, 0, 10);

        assertEquals(1, testBookings.size());

        List<BookingDto> testBookingStatusCurrent = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.CURRENT, 0, 10));

        assertEquals(1, testBookingStatusCurrent.size());
        assertEquals(1L, testBookingStatusCurrent.get(0).getId());
    }

    @Test
    void disApprovedBooking() throws ValidationException {
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusRejected = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.REJECTED, 0, 10));

        assertEquals(1, testBookingStatusRejected.size());
        assertEquals(3L, testBookingStatusRejected.get(0).getId());
    }

    @Test
    void approvedBooking_NotOwner() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.update(2L, 1L, "true"));
    }

    @Test
    void approvedBooking_InvalidUser() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.update(100L, 1L, "true"));
    }

    @Test
    void approvedBooking_InvalidBooking() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.update(1L, 100L, "true"));
    }

    @Test
    void approvedBooking_Duplicate() {
        bookingService.update(1L, 1L, "true");
        assertThrows(ValidationException.class, () -> bookingService.update(1L, 1L, "true"));
    }

    @Test
    void getBookingById() {
        BookingDto bookingDto = bookingService.getBooking(1L, 1L);

        assertEquals(1L, bookingDto.getId());
        assertEquals(1L, bookingDto.getItem().getId());
        assertEquals("test1", bookingDto.getBooker().getName());
        assertEquals(BookingStatus.WAITING, bookingDto.getStatus());
        assertTrue(LocalDateTime.now().isBefore(bookingDto.getEnd()));
        assertTrue(LocalDateTime.now().isAfter(bookingDto.getStart()));
    }

    @Test
    void getBookingById_InvalidUser() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(99L, 1L));
    }

    @Test
    void getBookingById_InvalidItem() {
        assertThrows(BookingNotFoundException.class, () -> bookingService.getBooking(1L, 111L));
    }

    @Test
    void findAllByUserByStateWaiting() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        Collection<BookingDto> testBookings = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.WAITING, 0, 10));

        assertEquals(1, testBookings.size());
    }

    @Test
    void findAllByUser_InvalidUser() {
        assertThrows(UserNotFoundException.class, () -> bookingService.findAllByUser(99L, BookingState.WAITING, 0, 10));
    }

    @Test
    void findAllByUserByStateRejected() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusRejected = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.REJECTED, 0, 10));

        assertEquals(1, testBookingStatusRejected.size());
        assertEquals(3L, testBookingStatusRejected.get(0).getId());
    }

    @Test
    void findAllByUserStateCurrent() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusCurrent = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.CURRENT, 0, 10));

        assertEquals(1, testBookingStatusCurrent.size());
        assertEquals(1L, testBookingStatusCurrent.get(0).getId());
    }

    @Test
    void findAllByUserByStateAll() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusAll = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.ALL, 0, 10));

        assertEquals(3, testBookingStatusAll.size());
        assertEquals(3L, testBookingStatusAll.get(0).getId());
        assertEquals(1L, testBookingStatusAll.get(1).getId());
        assertEquals(2L, testBookingStatusAll.get(2).getId());
    }

    @Test
    void findAllByUserByStateAll_invalidUser() {
        assertThrows(UserNotFoundException.class, () -> bookingService.findAllByUser(999L, BookingState.ALL, 0, 10));
    }

    @Test
    void findAllByOwnerBookingByStateAll() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusAll = new ArrayList<>(bookingService.findAllByOwner(1L, BookingState.ALL, 0, 10));

        assertEquals(3, testBookingStatusAll.size());
        assertEquals(3L, testBookingStatusAll.get(0).getId());
        assertEquals(1L, testBookingStatusAll.get(1).getId());
        assertEquals(2L, testBookingStatusAll.get(2).getId());
    }

    @Test
    void findAllByOwnerBookingByStateAll_InvalidUser() {
        assertThrows(UserNotFoundException.class, () -> bookingService.findAllByOwner(999L, BookingState.ALL, 0, 10));
    }

    @Test
    void findAllByUserByStatePast() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusPast = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.PAST, 0, 10));

        assertEquals(1, testBookingStatusPast.size());
        assertEquals(2L, testBookingStatusPast.get(0).getId());
    }

    @Test
    void findAllByUserByStateFuture() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusFuture = new ArrayList<>(bookingService.findAllByUser(2L, BookingState.FUTURE, 0, 10));

        assertEquals(1, testBookingStatusFuture.size());
        assertEquals(3L, testBookingStatusFuture.get(0).getId());
    }

    @Test
    void findAllByOwnerByStateWaiting() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookings = new ArrayList<>(bookingService.findAllByOwner(1L, BookingState.WAITING, 0, 10));

        assertEquals(1, testBookings.size());
        assertEquals(2L, testBookings.get(0).getId());
        assertEquals(1L, testBookings.get(0).getItem().getId());
    }

    @Test
    void findAllByOwner_InvalidUser() {
        assertThrows(UserNotFoundException.class, () -> bookingService.findAllByOwner(99L, BookingState.WAITING, 0, 10));
    }

    @Test
    void findAllByOwnerByStateRejected() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusRejected = new ArrayList<>(bookingService.findAllByOwner(1L, BookingState.REJECTED, 0, 10));

        assertEquals(1, testBookingStatusRejected.size());
        assertEquals(3L, testBookingStatusRejected.get(0).getId());
        assertEquals(1L, testBookingStatusRejected.get(0).getItem().getId());
    }

    @Test
    void findAllByOwnerByStateCurrent() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusCurrent = new ArrayList<>(bookingService.findAllByOwner(1L, BookingState.CURRENT, 0, 10));

        assertEquals(1, testBookingStatusCurrent.size());
        assertEquals(1L, testBookingStatusCurrent.get(0).getId());
        assertEquals(1L, testBookingStatusCurrent.get(0).getItem().getId());
    }

    @Test
    void findAllByOwnerByStatePast() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusPast = new ArrayList<>(bookingService.findAllByOwner(1L, BookingState.PAST, 0, 10));

        assertEquals(1, testBookingStatusPast.size());
        assertEquals(2L, testBookingStatusPast.get(0).getId());
        assertEquals(1L, testBookingStatusPast.get(0).getItem().getId());
    }

    @Test
    void findAllByOwnerByStateFuture() throws ValidationException {
        bookingService.update(1L, 1L, "true");
        bookingService.update(1L, 3L, "false");
        List<BookingDto> testBookingStatusFuture = new ArrayList<>(bookingService.findAllByOwner(1L, BookingState.FUTURE, 0, 10));

        assertEquals(1, testBookingStatusFuture.size());
        assertEquals(3L, testBookingStatusFuture.get(0).getId());
        assertEquals(1L, testBookingStatusFuture.get(0).getItem().getId());
    }
}