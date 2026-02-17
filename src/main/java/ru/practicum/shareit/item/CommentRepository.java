package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.author " +
            "WHERE c.item.id IN :itemIds")
    List<Comment> findByItemIdInWithAuthor(@Param("itemIds") List<Long> itemIds);
}