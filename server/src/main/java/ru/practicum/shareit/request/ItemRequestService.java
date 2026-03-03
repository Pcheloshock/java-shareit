package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, CreateItemRequestDto createDto);
    List<ItemRequestDto> getUserRequests(Long userId);
    List<ItemRequestDto> getAllRequests(Long userId);
    ItemRequestDto getRequestById(Long userId, Long requestId);
}
