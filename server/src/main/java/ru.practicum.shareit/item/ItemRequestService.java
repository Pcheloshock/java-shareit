package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CreateItemRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, CreateItemRequestDto createDto);

    List<ItemRequestDto> getUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}