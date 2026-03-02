package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingDto bookingDto);
    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);
    BookingDto getBookingById(Long userId, Long bookingId);
    List<BookingDto> getUserBookings(Long userId, BookingState state);
    List<BookingDto> getOwnerBookings(Long userId, BookingState state);
}
