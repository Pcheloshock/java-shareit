package ru.practicum.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.client.BaseClient;
import ru.practicum.item.dto.CreateCommentDto;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.CommentResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createItem(Long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemDto itemDto) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner(Long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok().build();
        }
        return get("/search?text={text}", null, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CreateCommentDto commentDto) {
        ResponseEntity<Object> response = post("/" + itemId + "/comment", userId, commentDto);
        return transformCommentResponse(response);
    }

    private ResponseEntity<Object> transformCommentResponse(ResponseEntity<Object> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return response;
        }

        try {
            Object body = response.getBody();
            JsonNode node = objectMapper.valueToTree(body);

            CommentResponseDto commentDto = CommentResponseDto.builder()
                    .id(node.has("id") ? node.get("id").asLong() : null)
                    .text(node.has("text") ? node.get("text").asText() : null)
                    .authorName(node.has("authorName") ? node.get("authorName").asText() : null)
                    .created(node.has("created") ? LocalDateTime.parse(node.get("created").asText(), DateTimeFormatter.ISO_DATE_TIME) : null)
                    .build();

            return ResponseEntity.status(response.getStatusCode()).body(commentDto);
        } catch (Exception e) {
            return response;
        }
    }
}
