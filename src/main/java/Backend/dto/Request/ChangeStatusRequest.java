package Backend.dto.Request;

import Backend.entity.Appointment.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequest {
    
    @NotNull(message = "Status is required")
    private AppointmentStatus status;
    
    private String notes; // Optional notes for status change
}
