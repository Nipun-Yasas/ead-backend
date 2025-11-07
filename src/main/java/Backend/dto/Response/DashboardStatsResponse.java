package Backend.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    private Long totalServices;
    private Long completedServices;
    private Long inProgressServices;
    private Long pendingServices;
    private Long todayAppointments;
    private Long cancelledServices;
    
    private List<StatusCount> servicesByStatus;
    private List<MonthlyTrend> monthlyTrend;
    private List<EmployeeWorkload> employeeWorkload;
    private List<UpcomingAppointment> upcomingAppointments;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCount {
        private String status;
        private Long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private Long value;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeWorkload {
        private String employeeName;
        private Long taskCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingAppointment {
        private String customerName;
        private String vehicleModel;
        private String serviceType;
        private String appointmentDate;
    }
}
