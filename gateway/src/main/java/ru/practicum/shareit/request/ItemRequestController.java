package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestAddDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequiredArgsConstructor
@RequestMapping("/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemRequestAddDto itemRequestAddDto) {
        return itemRequestClient.create(userId, itemRequestAddDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.findAllByOwner(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PositiveOrZero
            @RequestParam(defaultValue = "0") int from,
            @Positive
            @RequestParam(defaultValue = "10") int size) {
        return itemRequestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        return itemRequestClient.getRequest(userId, requestId);
    }
}
