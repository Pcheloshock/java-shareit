package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 3, 4, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 5, 10, 0);

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .id(2L)
                .name("Test User")
                .email("test@example.com")
                .build();

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(itemDto)
                .booker(userDto)
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).hasJsonPathMapValue("$.item");
        assertThat(result).hasJsonPathMapValue("$.booker");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\":1,\"start\":\"2026-03-04T10:00:00\",\"end\":\"2026-03-05T10:00:00\"," +
                "\"item\":{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\",\"available\":true}," +
                "\"booker\":{\"id\":2,\"name\":\"Test User\",\"email\":\"test@example.com\"}," +
                "\"status\":\"WAITING\"}";

        BookingDto bookingDto = objectMapper.readValue(content, BookingDto.class);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getItem().getId()).isEqualTo(1L);
        assertThat(bookingDto.getBooker().getId()).isEqualTo(2L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}
