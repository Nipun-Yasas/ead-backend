package Backend.dto.Response;

import java.time.LocalDateTime;

import Backend.entity.Message;
import lombok.Data;

@Data
public class MessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String senderEmail;  // ✅ Add this
    private String content;
    private Message.MessageType type;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean isEdited;
    private boolean isDeleted;
    
    // ✅ Add sender info as nested object (optional but helpful for frontend)
    private SenderInfo sender;
    
    @Data
    public static class SenderInfo {
        private Long id;
        private String fullName;
        private String email;
        private String role;
    }
}