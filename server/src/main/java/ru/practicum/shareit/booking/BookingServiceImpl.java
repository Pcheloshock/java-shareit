package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        return null;
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        return null;
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        return null;
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        return List.of();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        return List.of();
    }
}
