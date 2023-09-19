package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    ItemRequest itemRequest1;
    ItemRequest itemRequest2;
    ItemRequestDto itemRequestDto1;
    ItemRequestDto itemRequestDto2;

    @BeforeEach
    void createRequests() {
        LocalDateTime created1 = LocalDateTime.now();
        LocalDateTime created2 = LocalDateTime.now().plusHours(1);
        itemRequest1 = new ItemRequest(1L, "description1", null, created1);
        itemRequest2 = new ItemRequest(2L, "description2", null, created2);
        itemRequestDto1 = ItemRequestMapper.toItemRequestDto(itemRequest1, Collections.emptyList());
        itemRequestDto2 = ItemRequestMapper.toItemRequestDto(itemRequest2, Collections.emptyList());
    }

    @Test
    void createTest() throws Exception {
        when(itemRequestService.create(anyLong(), any())).thenReturn(itemRequestDto1);

        mockMvc.perform(
                        post("/requests")
                                .content(objectMapper.writeValueAsString(itemRequestDto1))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("description1"));
    }

    @Test
    void findAllByOwnerTest() throws Exception {
        Collection<ItemRequestDto> requests = List.of(itemRequestDto1);
        when(itemRequestService.findAllByOwner(anyLong())).thenReturn(requests);

        mockMvc.perform(
                        get("/requests")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(requests.size()));
    }

    @Test
    void findAllTest() throws Exception {
        Collection<ItemRequestDto> requests = List.of(itemRequestDto1, itemRequestDto2);
        when(itemRequestService.findAll(anyLong(), anyInt(), anyInt())).thenReturn(requests);

        mockMvc.perform(
                        get("/requests/all")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(requests.size()));
    }

    @Test
    void getRequestTest() throws Exception {
        when(itemRequestService.getRequest(anyLong(), anyLong())).thenReturn(itemRequestDto1);

        mockMvc.perform(
                        get("/requests/{requestId}", 1L)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("description1"));
    }
}
