package Backend.dto.Response;

import java.util.List;

public record AdminDashboardStatsResponse(
    Long totalServices,
    Long completedServices,
    Long inProgressServices,
    Long pendingServices,
    Long todayAppointments,
    Long cancelledServices,
    List<ServiceStatusCount> servicesByStatus,
    List<MonthlyServiceTrend> monthlyTrend,
    List<EmployeeWorkload> employeeWorkload,
    List<UpcomingAppointment> upcomingAppointments
) {}
