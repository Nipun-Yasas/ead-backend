package Backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Get all chats for a user (customer or employee)
     */
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

        return chats.stream()
                .map(this::convertToChatResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ UPDATED: Create or get existing chat between customer and employee
     * Returns ChatResponse instead of Chat entity
     * Used by AppointmentService for automatic chat creation
     */
    @Transactional
    public ChatResponse createOrGetChat(Long customerId, Long employeeId) {
        // Find customer
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        // Find employee
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Check if chat already exists
        Optional<Chat> existingChat = chatRepository.findByCustomerAndEmployee(customer, employee);

        Chat chat;
        if (existingChat.isPresent()) {
            // Chat already exists
            chat = existingChat.get();
            System.out.println("✅ Chat already exists - ID: " + chat.getId() + 
                             " (Customer: " + customerId + ", Employee: " + employeeId + ")");
        } else {
            // Create new chat
            Chat newChat = new Chat();
            newChat.setCustomer(customer);
            newChat.setEmployee(employee);
            newChat.setCreatedAt(LocalDateTime.now());
            newChat.setLastMessageAt(LocalDateTime.now());
            newChat.setLastMessageContent("Chat created for appointment allocation");
            
            chat = chatRepository.save(newChat);
            
            System.out.println("✅ New chat created - ID: " + chat.getId() + 
                             " (Customer: " + customerId + ", Employee: " + employeeId + ")");
        }

        // Convert to response and return
        return convertToChatResponse(chat);
    }

    /**
     * Update last message in chat
     */
    public void updateLastMessage(Long chatId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        chat.setLastMessageAt(LocalDateTime.now());
        chat.setLastMessageContent(content);
        chatRepository.save(chat);
    }

    /**
     * Convert Chat entity to ChatResponse DTO
     */
    private ChatResponse convertToChatResponse(Chat chat) {
        ChatResponse response = new ChatResponse();
        
        // Chat info
        response.setId(chat.getId());
        response.setCreatedAt(chat.getCreatedAt());
        response.setLastMessageAt(chat.getLastMessageAt());
        response.setLastMessageContent(chat.getLastMessageContent());
        
        // Customer info
        response.setCustomerId(chat.getCustomer().getId());
        response.setCustomerName(chat.getCustomer().getFullName());
        response.setCustomerEmail(chat.getCustomer().getEmail());
        
        // Employee info
        response.setEmployeeId(chat.getEmployee().getId());
        response.setEmployeeName(chat.getEmployee().getFullName());
        response.setEmployeeEmail(chat.getEmployee().getEmail());
        
        // Unread count (implement logic as needed)
        response.setUnreadCount(0); // TODO: Calculate from messages
        
        return response;
    }
}