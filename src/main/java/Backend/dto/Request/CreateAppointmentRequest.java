package Backend.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;

    private String vehicleType;

    private String vehicleNumber;

    @NotBlank(message = "Service type is required")
    private String serviceType;

    private String instructions;

    // Customer contact info for anonymous bookings
    private String customerName;

    private String customerEmail;

    private String customerPhone;
}