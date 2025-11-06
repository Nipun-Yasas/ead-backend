package Backend.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for allocating appointments to employees
 * Used by Task Allocation feature
 * Endpoint: PUT /api/appointments/{id}/allocate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAllocationRequest {
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
}