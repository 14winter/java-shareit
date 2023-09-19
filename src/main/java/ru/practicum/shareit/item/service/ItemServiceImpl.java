package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDtoBooking> findAllByOwner(Long userId, int from, int size) {
        log.info("Получен запрос на получение списка вещей пользователя по id {}", userId);
        User user = getUserById(userId);
        if (from < 0) {
            throw new ValidationException("Индекс страницы не может быть отрицательной.");
        }
        if (size < 1) {
            throw new ValidationException("Количество элементов не может быть отрицательной.");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<ItemDtoBooking> itemDtoBookings = new ArrayList<>();
        Map<Long, Item> itemMap = itemRepository.findAllByOwner(user, pageRequest).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<Booking> lastBookings = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndStatus(userId, LocalDateTime.now(),
                BookingStatus.APPROVED, Sort.by(Sort.Direction.DESC, "start"), pageRequest);
        Map<Long, List<Booking>> lastBookingMap = lastBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(),
                        mapping(booking -> booking, toList())));

        List<Booking> nextBookings = bookingRepository.findByItem_Owner_IdAndStartIsAfterAndStatus(userId, LocalDateTime.now(),
                BookingStatus.APPROVED, Sort.by(Sort.Direction.DESC, "start"), pageRequest);
        Map<Long, List<Booking>> nextBookingMap = nextBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(),
                        mapping(booking -> booking, toList())));

        Map<Long, List<CommentDto>> commentMap = commentRepository.findByItem_Owner_IdEquals(userId).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(),
                        mapping(CommentMapper::toCommentDto, toList())));

        for (Item item : itemMap.values()) {
            BookingDtoForItem lastBooking = null;
            BookingDtoForItem nextBooking = null;
            List<CommentDto> comment = new ArrayList<>();
            if (lastBookingMap.get(item.getId()) != null) {
                lastBooking = BookingMapper.toBookingDtoForItem(lastBookingMap.get(item.getId()).stream()
                        .sorted(Comparator.comparing(Booking::getStart)
                                .reversed()).collect(Collectors.toList()).get(0));
            }
            if (nextBookingMap.get(item.getId()) != null) {
                nextBooking = BookingMapper.toBookingDtoForItem(nextBookingMap.get(item.getId()).stream()
                        .sorted(Comparator.comparing(Booking::getStart)).collect(Collectors.toList()).get(0));
            }
            if (commentMap.containsKey(item.getId())) {
                comment = commentMap.get(item.getId());
            }
            itemDtoBookings.add(ItemMapper.toItemDtoBooking(item, lastBooking, nextBooking, comment));
        }
        return itemDtoBookings.stream().sorted(Comparator.comparing(ItemDtoBooking::getId)).collect(toList());
    }

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Получен запрос на создание вещи");
        Item item = ItemMapper.toItem(itemDto);
        if (item.getAvailable() == null) {
            throw new ValidationException("Нельзя создать вещь без статуса доступа");
        }
        item.setOwner(getUserById(userId));
        Item createdItem = itemRepository.save(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        log.info("Получен запрос на обновление вещи");
        Item updatedItem = getItemById(itemId);
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            updatedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            updatedItem.setDescription(itemDto.getDescription());
        }
        if ((itemDto.getAvailable() != null)) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }
        getUserById(userId);
        if (!updatedItem.getOwner().getId().equals(userId)) {
            log.info("У пользователя по id {} нет вещи с id {}", userId, itemId);
            throw new DataNotFoundException(itemId);
        }
        return ItemMapper.toItemDto(itemRepository.save(updatedItem));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoBooking getItem(Long userId, Long itemId) {
        log.info("Получен запрос на получение вещи");
        Item item = getItemById(itemId);
        getUserById(userId);
        ItemDtoBooking itemDtoBooking = ItemMapper.toItemDtoBooking(item, null, null, null);
        if (item.getOwner().getId().equals(userId)) {
            Booking lastBooking = bookingRepository.findLastBookingItem(itemId, LocalDateTime.now());
            if (lastBooking != null) {
                itemDtoBooking.setLastBooking(BookingMapper.toBookingDtoForItem(lastBooking));
            }
            Booking nextBooking = bookingRepository.findNextBookingItem(itemId, LocalDateTime.now());
            if (nextBooking != null) {
                itemDtoBooking.setNextBooking(BookingMapper.toBookingDtoForItem(nextBooking));
            }
        }
        Collection<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);
        if (comments != null) {
            itemDtoBooking.setComments(comments.stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        } else {
            itemDtoBooking.setComments(Collections.emptyList());
        }
        return itemDtoBooking;
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Получен запрос на комментирование от пользователя по id {}", userId);
        Item item = getItemById(itemId);
        User author = getUserById(userId);
        if (bookingRepository.findByItem_IdAndBooker_IdAndEndIsBefore(itemId, userId, LocalDateTime.now()).isEmpty()) {
            throw new NotAvailableException(String.format("Пользователь с id %d не брал в аренду вещь id %d, или аренда не завершена", userId, itemId));
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> search(Long userId, String text, int from, int size) {
        log.info("Получен запрос на поиск вещей от пользователя по id {}", userId);
        if (from < 0) {
            throw new ValidationException("Индекс страницы не может быть отрицательной.");
        }
        if (size < 1) {
            throw new ValidationException("Количество элементов не может быть отрицательной.");
        }
        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text, pageRequest).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("Пользователь с id {} не найден", userId);
                    return new UserNotFoundException(userId);
                });
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.info("Вещь с id {} не найдена", itemId);
                    return new DataNotFoundException(itemId);
                });
    }
}
