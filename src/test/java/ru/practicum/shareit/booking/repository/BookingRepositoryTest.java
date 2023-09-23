package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private BookingRepository repository;
    private final int from = 0;
    private final int size = 10;
    private final PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "end"));
    private final User user1 = User.builder().name("TestUser").email("test@email.com").build();
    private final User user2 = User.builder().name("TestUser1").email("test1@email.com").build();
    private final Item item1 = Item.builder().name("TestItem1").description("description1").available(true).owner(user1).build();
    private final Item item2 = Item.builder().name("TestItem2").description("description2").available(true).owner(user2).build();
    private final Booking booking1 = Booking.builder().start(LocalDateTime.now().minusDays(1)).end(LocalDateTime.now().plusDays(1)).item(item1).booker(user2).status(BookingStatus.APPROVED).build();
    private final Booking booking2 = Booking.builder().start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(5)).item(item2).booker(user1).status(BookingStatus.APPROVED).build();

    @BeforeEach
    public void beforeEach() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.flush();
    }

    @Test
    public void findByBooker_Id() {
        List<Booking> bookings = repository.findByBooker_Id(user1.getId(), pageRequest);
        Assertions.assertEquals(1, bookings.size());
        Assertions.assertEquals(booking2, bookings.get(0));
    }

    @Test
    public void findByBooker_IdAndEndIsBefore() {
        List<Booking> bookings = repository.findByBooker_IdAndEndIsBefore(
                user2.getId(),
                LocalDateTime.now().plusDays(5),
                pageRequest);
        Assertions.assertEquals(1, bookings.size());
        Assertions.assertEquals(booking1, bookings.get(0));
    }

    @Test
    public void findByBooker_IdAndStartIsAfter() {
        List<Booking> bookings = repository.findByBooker_IdAndStartIsAfter(
                user1.getId(),
                LocalDateTime.now(),
                pageRequest);
        Assertions.assertEquals(1, bookings.size());
        Assertions.assertEquals(booking2, bookings.get(0));
    }

    @Test
    public void findLastBookingItem() {
        Booking result = repository.findLastBookingItem(
                item1.getId(),
                LocalDateTime.now().plusDays(2));

        Assertions.assertEquals(booking1, result);
    }

    @Test
    public void findNextBookingItem() {
        Booking result = repository.findNextBookingItem(
                item2.getId(),
                LocalDateTime.now());

        Assertions.assertEquals(booking2, result);
    }
}
