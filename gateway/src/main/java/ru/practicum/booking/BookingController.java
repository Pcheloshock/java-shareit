package ru.practicum.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.booking.dto.BookingRequestDto;
import ru.practicum.booking.dto.BookingState;
import ru.practicum.exception.ValidationException;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("POST /bookings - создание бронирования пользователем ID: {}", userId);
        
        // Дополнительная валидация порядка дат
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart()) ||
            bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }
        
        return bookingClient.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{} - подтверждение бронирования", bookingId);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings - получение бронирований пользователя ID: {}", userId);
        return bookingClient.getUserBookings(userId, state);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("GET /bookings/{} - получение бронирования", bookingId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings/owner - получение бронирований владельца ID: {}", userId);
        return bookingClient.getOwnerBookings(userId, state);
    }
}
