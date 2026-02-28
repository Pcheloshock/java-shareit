package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId")
    List<Booking> findByItemOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.booker.id = :userId AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' AND b.end < :now")
    boolean hasUserBookedItem(@Param("userId") Long userId,
                              @Param("itemId") Long itemId,
                              @Param("now") LocalDateTime now);

    // Оптимизированные методы для получения последних и следующих бронирований для нескольких вещей
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' " +
            "AND b.start < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status")
    List<Booking> findByItemOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                             @Param("status") BookingStatus status,
                                             Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :now AND b.end > :now")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId,
                                       @Param("now") LocalDateTime now,
                                       Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.start < :now ORDER BY b.start DESC")
    List<Booking> findLastBookings(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = 'APPROVED' " +
            "AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookings(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.booker.id = :userId AND b.item.id = :itemId " +
            "AND b.status = :status AND b.end < :now")
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status);
}