package Backend.dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GenerateInvoiceRequest {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotEmpty(message = "Task breakdown is required")
    private List<TaskItem> taskBreakdown;
    
    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;
    
    private Boolean sendToCustomer = false; // Default false
    
    @Data
    public static class TaskItem {
        @NotNull(message = "Task name is required")
        private String taskName;
        
        @NotNull(message = "Price is required")
        private BigDecimal price;
    }
}
