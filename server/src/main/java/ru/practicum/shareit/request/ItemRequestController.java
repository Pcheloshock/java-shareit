package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody CreateItemRequestDto createDto) {
        log.info("POST /requests - создание запроса пользователем ID: {}", userId);
        return requestService.createRequest(userId, createDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests - получение запросов пользователя ID: {}", userId);
        return requestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithItemsDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /requests/all - получение всех запросов пользователем ID: {}", userId);
        return requestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithItemsDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long requestId) {
        log.info("GET /requests/{} - получение запроса пользователем ID: {}", requestId, userId);
        return requestService.getRequestById(userId, requestId);
    }
}
