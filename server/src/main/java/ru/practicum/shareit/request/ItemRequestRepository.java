package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(Long requesterId);

    @Query("SELECT r FROM ItemRequest r WHERE r.requester.id != :userId ORDER BY r.created DESC")
    List<ItemRequest> findAllOtherRequests(@Param("userId") Long userId);
}
