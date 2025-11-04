package Backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Backend.dto.Request.MessageRequest;
import Backend.dto.Response.MessageResponse;
import Backend.entity.Chat;
import Backend.entity.Message;
import Backend.entity.User;
import Backend.repository.ChatRepository;
import Backend.repository.MessageRepository;
import Backend.repository.UserRepository;

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
    
    @Transactional(readOnly = true)
    public List<MessageResponse> getChatMessages(Long chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // ✅ Fixed: Use standard findByChatId and filter deleted in stream
        Page<Message> messages = messageRepository.findByChatIdAndNotDeleted(chatId, pageable);

        return messages.getContent().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
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
        message.setCreatedAt(LocalDateTime.now());
        message.setEdited(false);
        message.setDeleted(false);
        
        // Handle custom question if present
        if (request.getCustomQuestionId() != null) {
            message.setCustomQuestionId(request.getCustomQuestionId());
        }
        
        Message savedMessage = messageRepository.save(message);

        // Update chat's last message
        chatService.updateLastMessage(chat.getId(), request.getContent());

        MessageResponse response = convertToMessageResponse(savedMessage);
        
        // ✅ Send real-time update via WebSocket with action type
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("action", "NEW_MESSAGE");
        wsMessage.put("message", response);
        
        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), wsMessage);
        
        return response;
    }
    
    @Transactional
    public MessageResponse editMessage(Long messageId, String newContent, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // ✅ Check authorization
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to edit this message");
        }
        
        // ✅ Check if message is deleted
        if (message.isDeleted()) {
            throw new RuntimeException("Cannot edit a deleted message");
        }
        
        // ✅ Removed restriction - allow multiple edits
        message.setContent(newContent);
        message.setEditedAt(LocalDateTime.now());
        message.setEdited(true);

        Message savedMessage = messageRepository.save(message);
        MessageResponse response = convertToMessageResponse(savedMessage);
        
        // ✅ Send real-time update via WebSocket with action type
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("action", "EDIT_MESSAGE");
        wsMessage.put("message", response);
        
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChat().getId(), wsMessage);
        
        return response;
    }
    
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // ✅ Check authorization
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this message");
        }
        
        // ✅ Check if already deleted
        if (message.isDeleted()) {
            throw new RuntimeException("Message already deleted");
        }
        
        Long chatId = message.getChat().getId();
        
        // ✅ Soft delete - mark as deleted instead of actually deleting
        message.setDeleted(true);
        message.setEditedAt(LocalDateTime.now()); // Track when deleted
        messageRepository.save(message);
        
        // ✅ Send real-time delete notification via WebSocket
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("action", "DELETE_MESSAGE");
        wsMessage.put("messageId", messageId);
        wsMessage.put("chatId", chatId);
        
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, wsMessage);
    }

    private MessageResponse convertToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setChatId(message.getChat().getId());
        response.setSenderId(message.getSender().getId());
        response.setSenderName(message.getSender().getFullName());
        response.setSenderEmail(message.getSender().getEmail()); 
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setCreatedAt(message.getCreatedAt());
        response.setEditedAt(message.getEditedAt());
        response.setEdited(message.isEdited());
        response.setDeleted(message.isDeleted());

        // ✅ Create sender info
        MessageResponse.SenderInfo senderInfo = new MessageResponse.SenderInfo();
        senderInfo.setId(message.getSender().getId());
        senderInfo.setFullName(message.getSender().getFullName());
        senderInfo.setEmail(message.getSender().getEmail());
        senderInfo.setRole(message.getSender().getRole().getName().toString());
        response.setSender(senderInfo);

        return response;
    }
}