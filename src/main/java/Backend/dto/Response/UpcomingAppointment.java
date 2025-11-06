package Backend.dto.Response;

import java.time.LocalDate;

public record UpcomingAppointment(
    String customerName,
    String vehicleModel,
    String service,
    LocalDate date
) {}
