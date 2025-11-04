package Backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Backend.dto.Response.ChatResponse;
import Backend.entity.Chat;
import Backend.entity.Role;
import Backend.entity.User;
import Backend.repository.ChatRepository;
import Backend.repository.UserRepository;

@Service
public class ChatService {
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<ChatResponse> getUserChats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Chat> chats;
        if (user.getRole().getName() == Role.RoleName.EMPLOYEE || 
            user.getRole().getName() == Role.RoleName.ADMIN) {
            chats = chatRepository.findByEmployeeOrderByLastMessageDesc(userId);
        } else {
            chats = chatRepository.findByCustomerOrderByLastMessageDesc(userId);
        }
        
        return chats.stream().map(this::convertToChatResponse).collect(Collectors.toList());
    }
    
    public Chat createOrGetChat(Long customerId, Long employeeId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        Optional<Chat> existingChat = chatRepository.findByCustomerAndEmployee(customer, employee);
        
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        
        Chat newChat = new Chat();
        newChat.setCustomer(customer);
        newChat.setEmployee(employee);
        newChat.setLastMessageAt(LocalDateTime.now());
        
        return chatRepository.save(newChat);
    }
    
    public void updateLastMessage(Long chatId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        chat.setLastMessageAt(LocalDateTime.now());
        chat.setLastMessageContent(content);
        chatRepository.save(chat);
    }
    
private ChatResponse convertToChatResponse(Chat chat) {
    ChatResponse response = new ChatResponse();
    response.setId(chat.getId());
    response.setCustomerId(chat.getCustomer().getId());
    response.setCustomerName(chat.getCustomer().getFullName());  // ✅ Fixed: Use getFullName()
    response.setCustomerEmail(chat.getCustomer().getEmail());
    response.setEmployeeId(chat.getEmployee().getId());
    response.setEmployeeName(chat.getEmployee().getFullName());  // ✅ Fixed: Use getFullName()
    response.setEmployeeEmail(chat.getEmployee().getEmail());
    response.setCreatedAt(chat.getCreatedAt());
    response.setLastMessageAt(chat.getLastMessageAt());
    response.setLastMessageContent(chat.getLastMessageContent());
    response.setUnreadCount(0);
    return response;
}
}