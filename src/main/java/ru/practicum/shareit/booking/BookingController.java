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
        System.out.println("=== CONTROLLER: approveBooking ===");
        System.out.println("userId: " + userId);
        System.out.println("bookingId: " + bookingId);
        System.out.println("approved: " + approved);

        try {
            BookingResponseDto result = bookingService.approveBooking(userId, bookingId, approved);
            System.out.println("Result: " + (result != null ? result.getId() : "null"));
            return ResponseEntity.ok(result);
        } catch (BookingNotFoundException e) {
            System.out.println("BookingNotFoundException: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (NotFoundException e) {
            System.out.println("NotFoundException: " + e.getMessage());
            return ResponseEntity.notFound().build();
            // } catch (ForbiddenException e) {  // УДАЛИТЕ ЭТОТ БЛОК
            //     System.out.println("ForbiddenException: " + e.getMessage());
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ValidationException e) {
            System.out.println("ValidationException: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @PathVariable Long bookingId) {
        System.out.println("=== CONTROLLER: getBookingById ===");
        System.out.println("userId: " + userId);
        System.out.println("bookingId: " + bookingId);

        try {
            BookingResponseDto result = bookingService.getBookingById(userId, bookingId);
            System.out.println("Result: " + (result != null ? result.getId() : "null"));
            return ResponseEntity.ok(result);
        } catch (NotFoundException e) {
            System.out.println("NotFoundException: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    @GetMapping("/debug/{bookingId}")
    public ResponseEntity<String> debugBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @PathVariable Long bookingId) {
        try {
            BookingResponseDto booking = bookingService.getBookingById(userId, bookingId);
            return ResponseEntity.ok("Booking found: " + booking.getId());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage() + ", type: " + e.getClass().getSimpleName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage() + ", type: " + e.getClass().getSimpleName());
        }
    }
}