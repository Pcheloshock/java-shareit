package ru.practicum.shareit.booking.dto;

public enum BookingStatus {
    WAITING,    // ожидает подтверждения
    APPROVED,   // подтверждено
    REJECTED,   // отклонено
    CANCELLED   // отменено
}