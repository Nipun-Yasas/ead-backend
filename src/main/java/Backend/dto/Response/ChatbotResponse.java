package Backend.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    private String answer;
    private String status;
    private Long timestamp;

    public ChatbotResponse(String answer) {
        this.answer = answer;
        this.status = "success";
        this.timestamp = System.currentTimeMillis();
    }
}
