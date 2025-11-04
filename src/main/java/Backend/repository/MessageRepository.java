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
    
    // ✅ ADD THIS - The method that MessageService.getChatMessages() needs
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByChatIdAndNotDeleted(@Param("chatId") Long chatId, Pageable pageable);
    
    // ✅ Keep your existing methods
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findByChatIdOrderByCreatedAtDesc(@Param("chatId") Long chatId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isDeleted = false")
    int countUnreadMessages(@Param("chatId") Long chatId, @Param("userId") Long userId);
    
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<Message> findAllByChatIdOrderByCreatedAtAsc(@Param("chatId") Long chatId);
    
    // ✅ ADD THIS - Standard method without deleted filter (useful for admin)
    Page<Message> findByChatId(Long chatId, Pageable pageable);
}