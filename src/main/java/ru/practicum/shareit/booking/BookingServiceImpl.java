package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        // Проверка пользователя
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Проверка вещи
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверка, что пользователь не владелец вещи
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        // Проверка дат
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException("Даты начала и окончания должны быть указаны");
        }

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ValidationException("Даты начала и окончания не могут совпадать");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        // Создание бронирования
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        System.out.println("=== SERVICE: approveBooking called ===");
        System.out.println("userId: " + userId);
        System.out.println("bookingId: " + bookingId);
        System.out.println("approved: " + approved);

        // Сначала проверяем существование бронирования
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    System.out.println("Booking not found with id: " + bookingId);
                    return new BookingNotFoundException("Бронирование не найдено");
                });

        System.out.println("Booking found: " + booking.getId());
        System.out.println("Booking status: " + booking.getStatus());
        System.out.println("Item ownerId: " + booking.getItem().getOwner().getId());

        // Проверка, что пользователь - владелец вещи
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            System.out.println("User " + userId + " is not owner. Owner is: " + booking.getItem().getOwner().getId());
            // ИСПРАВЛЕНО: теперь выбрасываем ForbiddenException вместо BookingNotFoundException
            throw new ForbiddenException("Только владелец может подтверждать бронирование");
        }

        // Проверка статуса
        if (booking.getStatus() != BookingStatus.WAITING) {
            System.out.println("Booking status is not WAITING: " + booking.getStatus());
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("Booking saved with status: " + savedBooking.getStatus());

        BookingResponseDto dto = BookingMapper.toBookingResponseDto(savedBooking);
        System.out.println("Returning dto with id: " + (dto != null ? dto.getId() : "null"));

        return dto;
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        System.out.println("=== SERVICE: getBookingById called ===");
        System.out.println("userId: " + userId);
        System.out.println("bookingId: " + bookingId);

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> {
                        System.out.println("Booking not found with id: " + bookingId);
                        return new NotFoundException("Бронирование не найдено");
                    });

            System.out.println("Booking found: " + booking.getId());
            System.out.println("Booker id: " + booking.getBooker().getId());
            System.out.println("Item owner id: " + booking.getItem().getOwner().getId());

            // Проверка, что пользователь - владелец вещи или автор бронирования
            if (!booking.getBooker().getId().equals(userId) &&
                    !booking.getItem().getOwner().getId().equals(userId)) {
                System.out.println("User " + userId + " has no access to this booking");
                throw new NotFoundException("Нет доступа к информации о бронировании");
            }

            BookingResponseDto dto = BookingMapper.toBookingResponseDto(booking);
            System.out.println("Returning dto with id: " + (dto != null ? dto.getId() : "null"));
            return dto;

        } catch (NotFoundException e) {
            System.out.println("NotFoundException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении информации о бронировании", e);
        }
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state) {
        // Проверка пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBefore(
                        userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(
                        userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, BookingState state) {
        // Проверка пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStatus() == BookingStatus.WAITING)
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                        .collect(Collectors.toList());
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

}