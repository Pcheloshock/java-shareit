package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CreateItemRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(CreateItemRequestDto dto, User requester, LocalDateTime created) {
        if (dto == null) {
            return null;
        }

        return ItemRequest.builder()
                .description(dto.getDescription())
                .requester(requester)
                .created(created)
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        List<ItemDto> itemDtos = request.getItems() != null
                ? request.getItems().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }
}