package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST /items - создание вещи пользователем ID: {}", userId);
        // Валидация
        if (itemRequestDto.getName() == null || itemRequestDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemRequestDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }
        // Конвертируем в ItemDto для сервиса
        ItemDto itemDto = ItemDto.builder()
                .name(itemRequestDto.getName())
                .description(itemRequestDto.getDescription())
                .available(itemRequestDto.getAvailable())
                .requestId(itemRequestDto.getRequestId())
                .build();
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CreateCommentDto commentDto) {
        log.info("POST /items/{}/comment - добавление комментария пользователем ID: {}", itemId, userId);
        // Валидация
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }
        return itemService.addComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemUpdateDto itemUpdateDto) {
        log.info("PATCH /items/{} - обновление вещи", itemId);
        // Конвертируем в ItemDto для сервиса (только переданные поля)
        ItemDto itemDto = ItemDto.builder()
                .name(itemUpdateDto.getName())
                .description(itemUpdateDto.getDescription())
                .available(itemUpdateDto.getAvailable())
                .build();
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable Long itemId) {
        log.info("GET /items/{} - получение вещи", itemId);
        return itemService.getItemWithBookingsById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("GET /items - получение вещей владельца ID: {}", ownerId);
        return itemService.getItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("GET /items/search - поиск вещей: {}", text);
        return itemService.searchItems(text);
    }
}
