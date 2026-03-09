package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Все бронирования пользователя
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    // Все бронирования для вещей владельца
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId);

    // Поиск последнего бронирования для вещи
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.start < :now ORDER BY b.start DESC")
    List<Booking> findLastBookingsForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Поиск следующего бронирования для вещи
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookingsForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    // Проверка, бронировал ли пользователь вещь
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = :status AND b.end < :now")
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status);
}
