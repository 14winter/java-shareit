package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingAddDto bookingAddDto) {
        log.info("Добавлен запрос на бронирование: {}", bookingAddDto);
        User user = getUserById(userId);
        Item item = itemRepository.findById(bookingAddDto.getItemId())
                .orElseThrow(() -> {
                    log.info("Вещь с id {} не найдена", bookingAddDto.getItemId());
                    return new DataNotFoundException(bookingAddDto.getItemId());
                });
        if (!item.getAvailable())
            throw new ValidationException("Вещь недоступна");
        if (isBookingAvailable(bookingAddDto)) {
            throw new ValidationException("Вещь недоступна в это время");
        }
        if (bookingAddDto.getStart().isAfter(bookingAddDto.getEnd()) || bookingAddDto.getStart().equals(bookingAddDto.getEnd()))
            throw new ValidationException("Неверно указано время");
        if (item.getOwner().getId().equals(userId))
            throw new BookingNotFoundException(String.format("Невозможно бронирование собственной вещи пользователем с id %d", userId));
        Booking booking = BookingMapper.toBooking(bookingAddDto);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto update(Long userId, Long bookingId, String approved) {
        log.info("Получен запрос на обновление бронирования с id {}", bookingId);
        Booking booking = getBookingById(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId))
            throw new BookingNotFoundException(String.format("Нет доступа для обновления у пользователя с id %d", userId));
        if (!booking.getStatus().equals(BookingStatus.WAITING))
            throw new ValidationException("Невозможно изменить статус");
        if (approved.equals("true")) {
            booking.setStatus(BookingStatus.APPROVED);
        } else booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBooking(Long userId, Long bookingId) {
        log.info("Получен запрос от пользователя с id {} об информации о бронировании с id {}", userId, bookingId);
        Booking booking = getBookingById(bookingId);
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new BookingNotFoundException(String.format("Пользователь с id %d не может запрашивать информацию о бронировании", userId));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingDto> findAllByUser(Long bookerId, BookingState state) {
        log.info("Получен запрос бронирований пользователя с id {}", bookerId);
        getUserById(bookerId);
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBooker_Id(bookerId,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case CURRENT:
                bookings = bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(bookerId, dateTime, dateTime,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case PAST:
                bookings = bookingRepository.findByBooker_IdAndEndIsBefore(bookerId, dateTime,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case FUTURE:
                bookings = bookingRepository.findByBooker_IdAndStartIsAfter(bookerId, dateTime,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case WAITING:
                bookings = bookingRepository.findByBooker_IdAndStatus(bookerId, BookingStatus.WAITING,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case REJECTED:
                bookings = bookingRepository.findByBooker_IdAndStatus(bookerId, BookingStatus.REJECTED,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<BookingDto> findAllByOwner(Long ownerId, BookingState state) {
        log.info("Получен запрос бронирования вещи пользователя с id {}", ownerId);
        getUserById(ownerId);
        LocalDateTime dateTime = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItem_Owner_Id(ownerId, Sort.by(Sort.Direction.DESC,
                        "start"));
                break;
            case CURRENT:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(ownerId,
                        dateTime, dateTime, Sort.by(Sort.Direction.DESC, "end"));
                break;
            case PAST:
                bookings = bookingRepository.findByItem_Owner_IdAndEndIsBefore(ownerId, dateTime,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case FUTURE:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsAfter(ownerId, dateTime,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case WAITING:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.WAITING,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
            case REJECTED:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.REJECTED,
                        Sort.by(Sort.Direction.DESC, "end"));
                break;
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Бронирование с id {} не найдено", id);
                    return new BookingNotFoundException(String.format("Бронирования с id %d не существует.", id));
                });
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь с id {} не найден", userId);
                    return new UserNotFoundException(userId);
                });
    }

    private boolean isBookingAvailable(BookingAddDto bookingAddDto) {
        Optional<Booking> bookingAvailability = bookingRepository.findFirstByItem_IdAndEndAfterAndStartBeforeAndStatus(
                bookingAddDto.getItemId(),
                bookingAddDto.getStart(),
                bookingAddDto.getEnd(),
                BookingStatus.APPROVED);
        return bookingAvailability.isPresent();
    }
}
