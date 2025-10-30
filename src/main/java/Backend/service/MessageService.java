package Backend.service;

import Backend.entity.Message;
import Backend.entity.Chat;
import Backend.entity.User;
import Backend.dto.Request.MessageRequest;
import Backend.dto.Response.MessageResponse;
import Backend.repository.MessageRepository;
import Backend.repository.ChatRepository;
import Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<MessageResponse> getChatMessages(Long chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByChatIdAndNotDeleted(chatId, pageable);

        return messages.getContent().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }

    public MessageResponse sendMessage(MessageRequest request) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setType(request.getType());

        Message savedMessage = messageRepository.save(message);

        // Update chat's last message
        chatService.updateLastMessage(chat.getId(), request.getContent());

        MessageResponse response = convertToMessageResponse(savedMessage);

        // Send real-time update via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), response);

        return response;
    }

    public MessageResponse editMessage(Long messageId, String newContent, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to edit this message");
        }

        if (message.isEdited()) {
            throw new RuntimeException("This message has already been edited and cannot be modified again");
        }

        message.setContent(newContent);
        message.setEditedAt(LocalDateTime.now());
        message.setEdited(true);

        Message savedMessage = messageRepository.save(message);
        MessageResponse response = convertToMessageResponse(savedMessage);

        // Send real-time update via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChat().getId(), response);

        return response;
    }

    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this message");
        }

        message.setDeleted(true);
        messageRepository.save(message);

        // Send real-time update via WebSocket
        MessageResponse response = convertToMessageResponse(message);
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChat().getId(), response);
    }

    private MessageResponse convertToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setChatId(message.getChat().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFullName());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setCreatedAt(message.getCreatedAt());
        response.setEditedAt(message.getEditedAt());
        response.setEdited(message.isEdited());
        response.setDeleted(message.isDeleted());
        return response;
    }
}
