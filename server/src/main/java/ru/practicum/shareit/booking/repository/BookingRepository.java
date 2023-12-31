package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_Id(Long bookerId, PageRequest pageRequest);

    List<Booking> findByBooker_IdAndStatus(Long userId, BookingStatus waiting, PageRequest pageRequest);

    List<Booking> findByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, PageRequest pageRequest);

    List<Booking> findByBooker_IdAndStartIsAfter(Long bookerId, LocalDateTime start, PageRequest pageRequest);

    List<Booking> findByItem_Owner_Id(Long ownerId, PageRequest pageRequest);

    List<Booking> findByItem_Owner_IdAndEndIsBefore(Long ownerId, LocalDateTime end, PageRequest pageRequest);

    List<Booking> findByItem_Owner_IdAndStartIsAfter(Long ownerId, LocalDateTime start, PageRequest pageRequest);

    List<Booking> findByItem_Owner_IdAndStatus(Long bookerId, BookingStatus status, PageRequest pageRequest);

    List<Booking> findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start, LocalDateTime end, PageRequest pageRequest);

    List<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, PageRequest pageRequest);

    @Query(nativeQuery = true,
            value = "SELECT * FROM bookings " +
                    "LEFT JOIN items i ON bookings.item_id = i.id " +
                    "WHERE item_id = ?1 " +
                    "AND start_date < ?2 " +
                    "ORDER BY end_date DESC " +
                    "LIMIT 1")
    Booking findLastBookingItem(Long itemId, LocalDateTime now);

    @Query(nativeQuery = true,
            value = "SELECT * FROM bookings " +
                    "LEFT JOIN items i ON bookings.item_id = i.id " +
                    "WHERE item_id = ?1 " +
                    "AND start_date > ?2 " +
                    "AND status = 'APPROVED' " +
                    "ORDER BY end_date " +
                    "LIMIT 1")
    Booking findNextBookingItem(Long itemId, LocalDateTime now);

    List<Booking> findByItem_IdAndBooker_IdAndEndIsBefore(Long itemId, Long bookerId, LocalDateTime end);

    List<Booking> findByItem_Owner_IdAndStartIsBeforeAndStatus(Long ownerId, LocalDateTime start, BookingStatus status, Sort sort, PageRequest pageRequest);

    List<Booking> findByItem_Owner_IdAndStartIsAfterAndStatus(Long ownerId, LocalDateTime start, BookingStatus status, Sort sort, PageRequest pageRequest);

    Optional<Booking> findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(Long itemId, LocalDateTime after, LocalDateTime before, BookingStatus status);
}
