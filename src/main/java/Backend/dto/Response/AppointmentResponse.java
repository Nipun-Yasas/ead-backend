package Backend.dto.Response;

import Backend.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private LocalDate date;
    private LocalTime time;
    private String vehicleType;
    private String vehicleNumber;
    private String service;
    private String instructions;
    private String status;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private CustomerInfo customer;
    private EmployeeInfo employee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Long id;
        private String fullName;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private Long id;
        private String fullName;
        private String email;
    }

    public static AppointmentResponse fromEntity(Appointment appointment) {
        AppointmentResponseBuilder builder = AppointmentResponse.builder()
                .id(appointment.getId())
                .date(appointment.getDate())
                .time(appointment.getTime())
                .vehicleType(appointment.getVehicleType())
                .vehicleNumber(appointment.getVehicleNumber())
                .service(appointment.getService())
                .instructions(appointment.getInstructions())
                .customerName(appointment.getCustomerName())
                .customerEmail(appointment.getCustomerEmail())
                .customerPhone(appointment.getCustomerPhone())
                .status(appointment.getStatus().name())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt());

        if (appointment.getCustomer() != null) {
            builder.customer(CustomerInfo.builder()
                    .id(appointment.getCustomer().getId())
                    .fullName(appointment.getCustomer().getFullName())
                    .email(appointment.getCustomer().getEmail())
                    .build());
        }

        if (appointment.getEmployee() != null) {
            builder.employee(EmployeeInfo.builder()
                    .id(appointment.getEmployee().getId())
                    .fullName(appointment.getEmployee().getFullName())
                    .email(appointment.getEmployee().getEmail())
                    .build());
        }

        return builder.build();
    }
}