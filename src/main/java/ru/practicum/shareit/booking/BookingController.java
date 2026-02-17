package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @RequestBody BookingDto bookingDto) {
        try {
            BookingResponseDto result = bookingService.createBooking(userId, bookingDto);
            return ResponseEntity.ok(result);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @PathVariable Long bookingId,
                                                             @RequestParam Boolean approved) {
        try {
            BookingResponseDto result = bookingService.approveBooking(userId, bookingId, approved);
            return ResponseEntity.ok(result);
        } catch (BookingNotFoundException e) {
            // Это исключение выбрасывается как для несуществующего бронирования,
            // так и для попытки подтверждения чужим пользователем
            return ResponseEntity.notFound().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @PathVariable Long bookingId) {
        try {
            BookingResponseDto result = bookingService.getBookingById(userId, bookingId);
            return ResponseEntity.ok(result);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                    @RequestParam(defaultValue = "ALL") BookingState state) {
        try {
            List<BookingResponseDto> result = bookingService.getUserBookings(userId, state);
            return ResponseEntity.ok(result);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                     @RequestParam(defaultValue = "ALL") BookingState state) {
        try {
            List<BookingResponseDto> result = bookingService.getOwnerBookings(userId, state);
            return ResponseEntity.ok(result);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}