package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, CreateItemRequestDto createDto);

    List<ItemRequestWithItemsDto> getUserRequests(Long userId);

    List<ItemRequestWithItemsDto> getAllRequests(Long userId);

    ItemRequestWithItemsDto getRequestById(Long userId, Long requestId);
}
