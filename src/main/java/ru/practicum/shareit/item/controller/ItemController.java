package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public Collection<ItemDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findAllByOwner(userId);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto, @PathVariable Long itemId) {
        return itemService.update(userId, itemDto, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam String text) {
        return itemService.search(userId, text);
    }
}
