package Backend.dto.Request;

import Backend.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    private LocalDate date;
    private LocalTime time;
    private String vehicleType;
    private String vehicleNumber;
    private String service;
    private String instructions;
    private Appointment.AppointmentStatus status;
    private Long employeeId;
}