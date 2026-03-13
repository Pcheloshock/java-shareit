package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createRequest_ValidInput_ReturnsOk() throws Exception {
        CreateItemRequestDto inputDto = new CreateItemRequestDto("Need a drill");
        ItemRequestDto outputDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        when(requestService.createRequest(eq(1L), any(CreateItemRequestDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getUserRequests_ReturnsList() throws Exception {
        List<ItemRequestWithItemsDto> requests = List.of(
                ItemRequestWithItemsDto.builder().id(1L).description("Request 1").created(LocalDateTime.now()).items(List.of()).build(),
                ItemRequestWithItemsDto.builder().id(2L).description("Request 2").created(LocalDateTime.now()).items(List.of()).build()
        );

        when(requestService.getUserRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAllRequests_ReturnsList() throws Exception {
        List<ItemRequestWithItemsDto> requests = List.of(
                ItemRequestWithItemsDto.builder().id(2L).description("Request 2").created(LocalDateTime.now()).items(List.of()).build()
        );

        when(requestService.getAllRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getRequestById_ValidId_ReturnsRequest() throws Exception {
        ItemRequestWithItemsDto request = ItemRequestWithItemsDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(requestService.getRequestById(1L, 1L)).thenReturn(request);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }
}
