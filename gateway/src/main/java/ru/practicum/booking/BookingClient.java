package ru.practicum.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.booking.dto.BookingRequestDto;
import ru.practicum.booking.dto.BookingState;
import ru.practicum.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

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
        return get("?state=" + state.name(), userId);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, BookingState state) {
        return get("/owner?state=" + state.name(), userId);
    }
}
