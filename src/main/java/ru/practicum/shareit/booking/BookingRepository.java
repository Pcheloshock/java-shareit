package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Поиск бронирований по пользователю (booker)
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Поиск бронирований по владельцу вещи
    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    // Поиск по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    // Для проверки существования бронирований пользователя на вещь
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.booker.id = :userId AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' AND b.end < :now")
    boolean hasUserBookedItem(@Param("userId") Long userId,
                              @Param("itemId") Long itemId,
                              @Param("now") LocalDateTime now);

    // Для получения последнего и следующего бронирования вещи
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.start < :now ORDER BY b.start DESC")
    List<Booking> findLastBookings(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookings(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);
}