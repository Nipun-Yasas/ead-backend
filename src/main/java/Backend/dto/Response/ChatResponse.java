package Backend.dto.Response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessageContent;
    private int unreadCount;
}