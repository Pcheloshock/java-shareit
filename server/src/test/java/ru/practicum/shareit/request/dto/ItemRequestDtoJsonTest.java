package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerialize() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 3, 3, 12, 0);

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(created)
                .build();

        JsonContent<ItemRequestDto> result = json.write(requestDto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2026-03-03T12:00:00\"}";

        ItemRequestDto requestDto = objectMapper.readValue(content, ItemRequestDto.class);

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.of(2026, 3, 3, 12, 0));
    }
}
