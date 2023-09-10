package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwner(User owner);

    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE (upper(i.name) LIKE UPPER(concat('%', ?1, '%')) " +
            "OR UPPER(i.description) LIKE UPPER(concat('%', ?1, '%'))) AND i.available != false")
    Collection<Item> search(String text);
}
