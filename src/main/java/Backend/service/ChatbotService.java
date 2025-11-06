package Backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@Slf4j
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ChatbotService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    private static final String SYSTEM_PROMPT = """
            You are an intelligent customer service assistant for an Automobile Service Management System.
            Your role is to help customers with:
            
            1. **Appointment Scheduling**: Guide users on how to book, reschedule, or cancel vehicle service appointments
            2. **Service Information**: Provide details about available services (repair, maintenance, inspection, diagnostics, etc.)
            3. **Vehicle Support**: Assist with queries about different vehicle types (cars, motorcycles, trucks, vans)
            4. **Appointment Status**: Help users check their appointment status and understand the workflow
            5. **Employee Assignment**: Explain how employees are assigned to appointments
            6. **Time Slots**: Inform about available time slots and booking procedures
            7. **Service Instructions**: Help users provide proper instructions for their vehicle service needs
            8. **System Navigation**: Guide users on how to use the system features
            
            **Important Guidelines:**
            - Be professional, friendly, and concise
            - Provide accurate information about automobile services
            - If asked about specific appointment details, remind users to check their dashboard or contact support
            - For technical vehicle issues, provide general guidance but recommend professional inspection
            - Always prioritize customer safety and proper vehicle maintenance
            - If you don't know something, be honest and suggest contacting support
            - Keep responses clear and easy to understand
            - Use bullet points or numbered lists for multiple steps
            
            **Available Services:**
            - Regular Maintenance (oil change, filter replacement, fluid checks)
            - Repairs (engine, transmission, brakes, suspension)
            - Inspections (safety, emissions, pre-purchase)
            - Diagnostics (computer diagnostics, problem identification)
            - Tire Services (rotation, alignment, replacement)
            - Electrical Services (battery, alternator, starter)
            
            **Appointment Status Explained:**
            - PENDING: Appointment submitted, awaiting approval
            - APPROVED: Appointment confirmed, employee assigned
            - REJECTED: Appointment cancelled or declined
            - COMPLETED: Service finished successfully
            
            Remember: You're here to make the customer's experience smooth and informative!
            """;

    public String generateResponse(String question, List<String> previousQuestions) {
        try {
            // Build context from previous questions
            StringBuilder contextBuilder = new StringBuilder(SYSTEM_PROMPT);
            contextBuilder.append("\n\n**Conversation History:**\n");
            
            if (previousQuestions != null && !previousQuestions.isEmpty()) {
                for (int i = 0; i < previousQuestions.size(); i++) {
                    contextBuilder.append(String.format("Previous Question %d: %s\n", i + 1, previousQuestions.get(i)));
                }
            }
            
            contextBuilder.append("\n**Current Question:** ").append(question);
            contextBuilder.append("\n\nProvide a helpful, accurate, and concise response:");

            String fullPrompt = contextBuilder.toString();

            // Call Gemini API
            String response = callGeminiAPI(fullPrompt);
            
            return response;

        } catch (Exception e) {
            log.error("Error generating chatbot response: {}", e.getMessage(), e);
            return "I apologize, but I'm experiencing technical difficulties at the moment. " +
                   "Please try again later or contact our support team for immediate assistance.";
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            // Log API key status (without exposing the actual key)
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                log.error("Gemini API key is not configured!");
                throw new RuntimeException("Gemini API key is missing. Please set GEMINI_API_KEY environment variable.");
            }
            
            log.debug("Gemini API key is configured (length: {})", geminiApiKey.length());
            
            String apiUrl = GEMINI_API_URL + "?key=" + geminiApiKey;
            
            // Build request JSON matching the working Node.js approach
            String requestBody = String.format("""
                {
                  "contents": [{
                    "role": "user",
                    "parts": [{
                      "text": %s
                    }]
                  }]
                }
                """, objectMapper.writeValueAsString(prompt));

            log.debug("Sending request to Gemini API: {}", apiUrl.replace(geminiApiKey, "***"));
            
            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());

            log.debug("Gemini API response status: {}", response.statusCode());
            
            // Parse response
            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            } else {
                log.error("Gemini API error: Status code {}, Response: {}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API returned error: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // Navigate through JSON structure: candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    String text = firstPart.path("text").asText();
                    
                    if (text != null && !text.isEmpty()) {
                        return text;
                    }
                }
            }
            
            log.error("Failed to parse Gemini response: {}", responseBody);
            return "I received a response but couldn't process it properly. Please try rephrasing your question.";
            
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            return "Error processing the response. Please try again.";
        }
    }
}
