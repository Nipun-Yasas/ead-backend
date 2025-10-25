package Backend.dto.Request;

import Backend.entity.Message;
import lombok.Data;

@Data
public class MessageRequest {
    private Long chatId;
    private Long senderId;
    private String content;
    private Message.MessageType type = Message.MessageType.TEXT;
    private Long customQuestionId;
}