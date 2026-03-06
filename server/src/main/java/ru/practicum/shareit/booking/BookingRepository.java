package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' AND b.start < :now ORDER BY b.start DESC")
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = :status AND b.end < :now")
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status);
}
