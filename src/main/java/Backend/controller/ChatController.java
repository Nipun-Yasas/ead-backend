// filepath: c:\Users\Jehan\Desktop\react projects\ead-backend\src\main\java\Backend\controller\ChatController.java
package Backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import Backend.dto.Request.MessageRequest;
import Backend.service.ChatService;
import Backend.service.CustomQuestionService;
import Backend.service.MessageService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "${FRONTEND_URL}")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private CustomQuestionService customQuestionService;
    
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getUserChats(userId));
    }
    
    @GetMapping("/messages/{chatId}")
    public ResponseEntity<?> getChatMessages(@PathVariable Long chatId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.getChatMessages(chatId, page, size));
    }
    
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }
    
    @PutMapping("/edit/{messageId}")
    public ResponseEntity<?> editMessage(@PathVariable Long messageId, 
                                       @RequestBody String newContent,
                                       @RequestParam Long userId) {
        return ResponseEntity.ok(messageService.editMessage(messageId, newContent, userId));
    }
    
    @DeleteMapping("/delete/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId,
                                         @RequestParam Long userId) {
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestParam Long customerId, 
                                      @RequestParam Long employeeId) {
        return ResponseEntity.ok(chatService.createOrGetChat(customerId, employeeId));
    }
    
    @GetMapping("/custom-questions")
    public ResponseEntity<?> getCustomQuestions() {
        return ResponseEntity.ok(customQuestionService.getAllActiveQuestions());
    }
}