package ru.practicum.shareit.item;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequesterId(Long requesterId, Sort sort);

    @Query("SELECT r FROM ItemRequest r " +
            "LEFT JOIN FETCH r.items " +
            "WHERE r.requester.id = :requesterId")
    List<ItemRequest> findByRequesterIdWithItems(@Param("requesterId") Long requesterId, Sort sort);

    @Query("SELECT r FROM ItemRequest r " +
            "LEFT JOIN FETCH r.items " +
            "WHERE r.requester.id != :userId")
    List<ItemRequest> findAllExceptUserWithItems(@Param("userId") Long userId, Sort sort);

    @Query("SELECT r FROM ItemRequest r " +
            "LEFT JOIN FETCH r.items " +
            "WHERE r.id = :requestId")
    ItemRequest findByIdWithItems(@Param("requestId") Long requestId);
}