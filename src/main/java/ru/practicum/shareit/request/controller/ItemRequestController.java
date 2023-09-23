package ru.practicum.shareit.request.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Validated
@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemRequestAddDto itemRequestAddDto) {
        return itemRequestService.create(userId, itemRequestAddDto);
    }

    @GetMapping
    public Collection<ItemRequestDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findAllByOwner(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> findAll(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PositiveOrZero
            @RequestParam(defaultValue = "0") int from,
            @Positive
            @RequestParam(defaultValue = "10") int size) {
        return itemRequestService.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        return itemRequestService.getRequest(userId, requestId);
    }
}
