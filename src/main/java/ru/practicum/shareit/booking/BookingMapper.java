package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {

    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(bookingDto.getStatus() != null ? bookingDto.getStatus() : BookingStatus.WAITING)
                .build();
    }

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto.BookingDtoBuilder builder = BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus());

        // Для ответа всегда включаем полные объекты
        if (booking.getItem() != null) {
            builder.item(ItemMapper.toItemDto(booking.getItem()));
            builder.itemId(booking.getItem().getId()); // Для обратной совместимости
        }

        if (booking.getBooker() != null) {
            builder.booker(UserMapper.toUserDto(booking.getBooker()));
            builder.bookerId(booking.getBooker().getId()); // Для обратной совместимости
        }

        return builder.build();
    }

    // Метод для создания из запроса (когда нужны только ID)
    public static BookingDto toSimpleDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .status(booking.getStatus())
                .build();
    }
}