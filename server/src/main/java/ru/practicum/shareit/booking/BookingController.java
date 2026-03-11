package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody BookingRequestDto bookingRequestDto) {
        log.info("POST /bookings - создание бронирования");
        
        // Конвертируем BookingRequestDto в BookingDto для сервиса
        BookingDto bookingDto = BookingDto.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .itemId(bookingRequestDto.getItemId())
                .build();
                
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{} - подтверждение бронирования", bookingId);
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId) {
        log.info("GET /bookings/{} - получение бронирования", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings - получение бронирований пользователя");
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings/owner - получение бронирований владельца");
        return bookingService.getOwnerBookings(userId, state);
    }
}
