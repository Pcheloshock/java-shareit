package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class CommentController {
    private final ItemService itemService;

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CreateCommentDto commentDto) {
        log.info("POST /items/{}/comment - добавление комментария пользователем ID: {}", itemId, userId);
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ru.practicum.shareit.exception.ValidationException("Текст комментария не может быть пустым");
        }
        return itemService.addComment(userId, itemId, commentDto);
    }
}
