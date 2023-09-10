package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface ItemRepository {
    Collection<Item> findAllByOwner(User owner);

    Item create(Long userId, Item item);

    Item update(Item item);

    Item getItem(Long id);

    Collection<Item> search(String text);
}
