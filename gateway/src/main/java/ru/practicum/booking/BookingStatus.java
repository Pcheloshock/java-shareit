package ru.practicum.booking;

public enum BookingStatus {
    WAITING,    // ожидает подтверждения
    APPROVED,   // подтверждено
    REJECTED,   // отклонено
    CANCELLED   // отменено
}