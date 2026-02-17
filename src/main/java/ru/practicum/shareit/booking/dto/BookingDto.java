package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;

    // Для создания бронирования (входной DTO)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long itemId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long bookerId;

    // Для ответа (выходной DTO)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ItemDto item;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserDto booker;

    private BookingStatus status;
}