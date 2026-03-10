package ru.practicum.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.booking.dto.BookingRequestDto;
import ru.practicum.booking.dto.BookingResponseDto;
import ru.practicum.booking.dto.BookingState;
import ru.practicum.client.BaseClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        return post("", userId, bookingRequestDto);
    }

    public ResponseEntity<Object> approveBooking(Long userId, Long bookingId, Boolean approved) {
        return patch("/" + bookingId + "?approved=" + approved, userId, null);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, BookingState state) {
        ResponseEntity<Object> response = get("?state=" + state.name(), userId);
        return transformResponse(response);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        ResponseEntity<Object> response = get("/" + bookingId, userId);
        return transformResponse(response);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, BookingState state) {
        ResponseEntity<Object> response = get("/owner?state=" + state.name(), userId);
        return transformResponse(response);
    }

    private ResponseEntity<Object> transformResponse(ResponseEntity<Object> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return response;
        }

        try {
            Object body = response.getBody();
            if (body instanceof List) {
                List<BookingResponseDto> result = new ArrayList<>();
                for (Object item : (List<?>) body) {
                    BookingResponseDto dto = transformToFlatDto(item);
                    if (dto != null) {
                        result.add(dto);
                    }
                }
                return ResponseEntity.status(response.getStatusCode()).body(result);
            } else {
                BookingResponseDto result = transformToFlatDto(body);
                return ResponseEntity.status(response.getStatusCode()).body(result);
            }
        } catch (Exception e) {
            return response;
        }
    }

    private BookingResponseDto transformToFlatDto(Object item) {
        try {
            JsonNode node = objectMapper.valueToTree(item);

            Long id = node.has("id") ? node.get("id").asLong() : null;

            LocalDateTime start = null;
            if (node.has("start")) {
                start = LocalDateTime.parse(node.get("start").asText(), DateTimeFormatter.ISO_DATE_TIME);
            }

            LocalDateTime end = null;
            if (node.has("end")) {
                end = LocalDateTime.parse(node.get("end").asText(), DateTimeFormatter.ISO_DATE_TIME);
            }

            String status = node.has("status") ? node.get("status").asText() : null;

            Long itemId = null;
            Long bookerId = null;

            if (node.has("item") && node.get("item").has("id")) {
                itemId = node.get("item").get("id").asLong();
            }

            if (node.has("booker") && node.get("booker").has("id")) {
                bookerId = node.get("booker").get("id").asLong();
            }

            return BookingResponseDto.builder()
                    .id(id)
                    .start(start)
                    .end(end)
                    .itemId(itemId)
                    .bookerId(bookerId)
                    .status(status != null ? BookingState.valueOf(status) : null)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
