package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long userId, BookingStatus waiting, Sort sort);

    List<Booking> findByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByItem_Owner_IdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

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
}
