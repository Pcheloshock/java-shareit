package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {

    public static Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
    }

    public static BookingDto toBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .status(booking.getStatus())
                .build();
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        System.out.println("Mapping booking: " + booking.getId());

        BookingResponseDto.BookingResponseDtoBuilder builder = BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus());

        if (booking.getItem() != null) {
            builder.item(ItemMapper.toItemDto(booking.getItem()));
            System.out.println("Item mapped: " + booking.getItem().getId());
        }

        if (booking.getBooker() != null) {
            builder.booker(UserMapper.toUserDto(booking.getBooker()));
            System.out.println("Booker mapped: " + booking.getBooker().getId());
        }

        BookingResponseDto dto = builder.build();
        System.out.println("Dto created with id: " + dto.getId());
        return dto;
    }
}