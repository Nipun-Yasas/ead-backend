package Backend.controller;

import Backend.dto.Request.MessageRequest;
import Backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    
    @Autowired
    private MessageService messageService;
    
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest messageRequest) {
        messageService.sendMessage(messageRequest);
    }
}