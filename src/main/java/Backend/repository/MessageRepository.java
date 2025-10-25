package Backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Backend.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    Page<Message> findByChatIdAndNotDeleted(@Param("chatId") Long chatId, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> findByChatIdOrderByCreatedAtDesc(@Param("chatId") Long chatId);
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<Message> findByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);
}