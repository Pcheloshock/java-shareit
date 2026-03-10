package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        log.info("Создание бронирования пользователем ID: {}", userId);

        // Определяем ID вещи (может быть в itemId или в item.getId())
        Long itemId = null;
        if (bookingDto.getItemId() != null) {
            itemId = bookingDto.getItemId();
        } else if (bookingDto.getItem() != null && bookingDto.getItem().getId() != null) {
            itemId = bookingDto.getItem().getId();
        }

        if (itemId == null) {
            throw new ValidationException("Не указан ID вещи");
        }

        // Проверяем пользователя
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Проверяем вещь
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверяем, что владелец не бронирует свою вещь
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }

        // Проверяем доступность вещи
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        // Проверяем даты
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException("Даты начала и окончания должны быть указаны");
        }

        LocalDateTime now = LocalDateTime.now();
        if (bookingDto.getStart().isBefore(now)) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
            bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть после даты начала");
        }

        // Создаем бронирование
        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование успешно создано с ID: {}", savedBooking.getId());

        return mapToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение бронирования ID: {} пользователем ID: {}, approved: {}", bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование {} успешно обновлено", bookingId);

        return mapToDto(savedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.info("Получение информации о бронировании ID: {} пользователем ID: {}", bookingId, userId);

        // Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступа к информации о бронировании");
        }

        return mapToDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        log.info("Получение бронирований пользователя ID: {} со статусом: {}", userId, state);

        // Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);

        return filterBookingsByState(bookings, state, now).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        log.info("Получение бронирований вещей владельца ID: {} со статусом: {}", userId, state);

        // Проверяем существование пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByItemOwnerId(userId);

        return filterBookingsByState(bookings, state, now).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filterBookingsByState(List<Booking> bookings, BookingState state, LocalDateTime now) {
        switch (state) {
            case CURRENT:
                return bookings.stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
            case PAST:
                return bookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
            case FUTURE:
                return bookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
            case WAITING:
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.WAITING)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                        .collect(Collectors.toList());
            default:
                return bookings;
        }
    }

    private BookingDto mapToDto(Booking booking) {
        ItemDto itemDto = ItemDto.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .description(booking.getItem().getDescription())
                .available(booking.getItem().getAvailable())
                .build();

        UserDto userDto = UserDto.builder()
                .id(booking.getBooker().getId())
                .name(booking.getBooker().getName())
                .email(booking.getBooker().getEmail())
                .build();

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemDto)
                .booker(userDto)
                .status(booking.getStatus())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
