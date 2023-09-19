package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    private ItemDto itemDto;
    private ItemDtoBooking itemDtoBooking;
    private CommentDto commentDto;

    @BeforeEach
    void beforeEach() {
        User user = new User(1L, "user", "user@email.com");
        Item item = new Item(1L, "item", "description", true, null, null);
        itemDto = ItemMapper.toItemDto(item);
        itemDtoBooking = ItemMapper.toItemDtoBooking(item, null, null, null);
        Comment comment = new Comment(1L, "comment", item, user, LocalDateTime.now());
        commentDto = CommentMapper.toCommentDto(comment);
    }

    @Test
    void createItemTest() throws Exception {
        when(itemService.create(any(), any()))
                .thenReturn(itemDto);

        mockMvc.perform(
                        post("/items")
                                .content(objectMapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()));
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.update(anyLong(), any(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(
                        patch("/items/{itemId}", itemDto.getId())
                                .content(objectMapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()));
    }

    @Test
    void getItemByIdTest() throws Exception {
        when(itemService.getItem(anyLong(), anyLong()))
                .thenReturn(itemDtoBooking);
        mockMvc.perform(
                        get("/items/{itemId}", 1L)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDtoBooking.getName()))
                .andExpect(jsonPath("$.description").value(itemDtoBooking.getDescription()));
    }

    @Test
    void findAllByOwnerTest() throws Exception {
        Collection<ItemDtoBooking> items = List.of(itemDtoBooking);
        when(itemService.findAllByOwner(anyLong(), anyInt(), anyInt()))
                .thenReturn(items);
        mockMvc.perform(
                        get("/items")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(items.size()));
    }

    @Test
    void searchTest() throws Exception {
        Collection<ItemDto> items = List.of(itemDto);
        when(itemService.search(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(items);
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("item")));
    }

    @Test
    void createCommentToItem() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mockMvc.perform(
                        post("/items/{itemId}/comment", 1L)
                                .content(objectMapper.writeValueAsString(commentDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));
    }
}
