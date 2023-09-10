package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Collection<ItemDtoBooking> findAllByOwner(Long userId) {
        log.info("Получен запрос на получение списка вещей пользователя по id {}", userId);
        User user = getUserById(userId);
        List<Item> items = itemRepository.findAllByOwner(user);
        items.sort(Comparator.comparing(Item::getId));
        List<ItemDtoBooking> itemsDtoBooking = items.stream()
                .map(ItemMapper::toItemDtoBooking)
                .collect(Collectors.toList());
        for (int i = 0; i < itemsDtoBooking.size(); i++) {
            Booking lastBooking = bookingRepository.findLastBookingItem(items.get(i).getId(), LocalDateTime.now());
            if (lastBooking != null) {
                itemsDtoBooking.get(i).setLastBooking(BookingMapper.toBookingDtoForItem(lastBooking));
            }
            Booking nextBooking = bookingRepository.findNextBookingItem(items.get(i).getId(), LocalDateTime.now());
            if (nextBooking != null) {
                itemsDtoBooking.get(i).setNextBooking(BookingMapper.toBookingDtoForItem(nextBooking));
            }
            Collection<Comment> comments = commentRepository.findCommentsByItem_Id(itemsDtoBooking.get(i).getId());
            if (comments != null) {
                itemsDtoBooking.get(i).setComments(
                        comments.stream()
                                .map(CommentMapper::toCommentDto)
                                .collect(Collectors.toList()));
            } else {
                itemsDtoBooking.get(i).setComments(Collections.emptyList());
            }
        }
        return itemsDtoBooking;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Получен запрос на создание вещи");
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(getUserById(userId));
        if (item.getAvailable() == null) {
            throw new ValidationException("Нельзя создать вещь без статуса доступа");
        }
        Item createdItem = itemRepository.save(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        log.info("Получен запрос на обновление вещи");
        getUserById(userId);
        Item updatedItem = getItemById(itemId);

        if (!updatedItem.getOwner().getId().equals(userId)) {
            log.info("У пользователя по id {} нет вещи с id {}", userId, itemId);
            throw new DataNotFoundException(itemId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            updatedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            updatedItem.setDescription(itemDto.getDescription());
        }
        if ((itemDto.getAvailable() != null)) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(updatedItem));
    }

    @Override
    public ItemDtoBooking getItem(Long userId, Long itemId) {
        log.info("Получен запрос на получение вещи");
        getUserById(userId);
        Item item = getItemById(itemId);
        ItemDtoBooking itemDtoBooking = ItemMapper.toItemDtoBooking(item);
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
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Получен запрос на комментирование от пользователя по id {}", userId);
        User author = getUserById(userId);
        Item item = getItemById(itemId);
        if (bookingRepository.findByItem_IdAndBooker_IdAndEndIsBefore(itemId, userId, LocalDateTime.now()).isEmpty()) {
            throw new ValidationException(String.format("Пользователь с id %d не брал в аренду вещь id %d, или аренда не завершена", userId, itemId));
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public Collection<ItemDto> search(Long userId, String text) {
        log.info("Получен запрос на поиск вещей от пользователя по id {}", userId);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
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
