package Backend.controller;

import Backend.dto.Request.ChatbotRequest;
import Backend.dto.Response.ChatbotResponse;
import Backend.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * Chatbot endpoint to get AI-powered responses
     * 
     * @param request Contains the current question and previous questions for context
     * @return AI-generated response
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponse> askQuestion(@RequestBody ChatbotRequest request) {
        try {
            log.info("Received chatbot question: {}", request.getQuestion());
            
            // Validate request
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ChatbotResponse(
                                "Please provide a question.",
                                "error",
                                System.currentTimeMillis()
                        ));
            }

            // Generate response using Gemini AI
            String answer = chatbotService.generateResponse(
                    request.getQuestion(), 
                    request.getPreviousQuestions()
            );

            ChatbotResponse response = new ChatbotResponse(answer);
             log.info("Generated chatbot response successfully", response);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chatbot request: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ChatbotResponse(
                            "I'm sorry, I encountered an error while processing your request. Please try again.",
                            "error",
                            System.currentTimeMillis()
                    ));
        }
    }

    /**
     * Health check endpoint for chatbot service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
