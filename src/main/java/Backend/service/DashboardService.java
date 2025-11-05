package Backend.service;

import Backend.dto.Response.*;
import Backend.entity.Service.ServiceStatus;
import Backend.repository.AppointmentRepository;
import Backend.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    
    public AdminDashboardStatsResponse getDashboardStats() {
        Long totalServices = serviceRepository.count();
        Long completedServices = serviceRepository.countByStatus(ServiceStatus.COMPLETED);
        Long inProgressServices = serviceRepository.countByStatus(ServiceStatus.IN_PROGRESS);
        Long pendingServices = serviceRepository.countByStatus(ServiceStatus.PENDING);
        Long todayAppointments = appointmentRepository.countByDate(LocalDate.now());
        
        List<ServiceStatusCount> servicesByStatus = getServicesByStatus();
        List<MonthlyServiceTrend> monthlyTrend = getMonthlyTrend();
        List<EmployeeWorkload> employeeWorkload = getEmployeeWorkload();
        List<UpcomingAppointment> upcomingAppointments = getUpcomingAppointments();
        
        return new AdminDashboardStatsResponse(
            totalServices,
            completedServices,
            inProgressServices,
            pendingServices,
            todayAppointments,
            servicesByStatus,
            monthlyTrend,
            employeeWorkload,
            upcomingAppointments
        );
    }
    
    private List<ServiceStatusCount> getServicesByStatus() {
        return serviceRepository.countByStatusGrouped().stream()
            .map(result -> new ServiceStatusCount(
                result[0].toString(),
                ((Number) result[1]).longValue()
            ))
            .collect(Collectors.toList());
    }
    
    private List<MonthlyServiceTrend> getMonthlyTrend() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        return serviceRepository.getMonthlyTrend(sixMonthsAgo).stream()
            .map(result -> new MonthlyServiceTrend(
                result[0].toString(),
                ((Number) result[1]).intValue()
            ))
            .collect(Collectors.toList());
    }
    
    private List<EmployeeWorkload> getEmployeeWorkload() {
        return serviceRepository.getEmployeeWorkload(null).stream()
            .limit(3)
            .map(result -> new EmployeeWorkload(
                result[0].toString(),
                ((Number) result[1]).longValue()
            ))
            .collect(Collectors.toList());
    }
    
    private List<UpcomingAppointment> getUpcomingAppointments() {
        return appointmentRepository.findUpcomingAppointments(LocalDate.now()).stream()
            .limit(3)
            .map(appointment -> new UpcomingAppointment(
                appointment.getCustomer() != null ? appointment.getCustomer().getFullName() : appointment.getCustomerName(),
                appointment.getVehicleType(),
                appointment.getService(),
                appointment.getDate()
            ))
            .collect(Collectors.toList());
    }
}