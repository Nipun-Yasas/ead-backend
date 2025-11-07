package Backend.service;

import Backend.dto.Response.*;
import Backend.entity.Appointment;
import Backend.entity.Appointment.AppointmentStatus;
import Backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final AppointmentRepository appointmentRepository;
    
    public AdminDashboardStatsResponse getDashboardStats() {
        // Get all appointments
        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // Calculate statistics
        Long totalServices = (long) allAppointments.size();
        Long completedServices = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
            .count();
        Long inProgressServices = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.IN_PROGRESS)
            .count();
        Long pendingServices = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.PENDING)
            .count();
        Long cancelledServices = allAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.REJECT)
            .count();
        Long todayAppointments = appointmentRepository.countByDate(LocalDate.now());
        
        List<ServiceStatusCount> servicesByStatus = getServicesByStatus(allAppointments);
        List<MonthlyServiceTrend> monthlyTrend = getMonthlyTrend(allAppointments);
        List<EmployeeWorkload> employeeWorkload = getEmployeeWorkload(allAppointments);
        List<UpcomingAppointment> upcomingAppointments = getUpcomingAppointments();
        
        return new AdminDashboardStatsResponse(
            totalServices,
            completedServices,
            inProgressServices,
            pendingServices,
            todayAppointments,
            cancelledServices,
            servicesByStatus,
            monthlyTrend,
            employeeWorkload,
            upcomingAppointments
        );
    }
    
    private List<ServiceStatusCount> getServicesByStatus(List<Appointment> appointments) {
        Map<AppointmentStatus, Long> statusCounts = appointments.stream()
            .collect(Collectors.groupingBy(
                Appointment::getStatus,
                Collectors.counting()
            ));
        
        return statusCounts.entrySet().stream()
            .map(entry -> new ServiceStatusCount(
                entry.getKey().toString(),
                entry.getValue()
            ))
            .collect(Collectors.toList());
    }
    
    private List<MonthlyServiceTrend> getMonthlyTrend(List<Appointment> appointments) {
        // Get current date
        LocalDate now = LocalDate.now();
        
        // Get last 12 months
        List<MonthlyServiceTrend> trends = new ArrayList<>();
        
        for (int i = 11; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();
            
            long count = appointments.stream()
                .filter(a -> !a.getDate().isBefore(startOfMonth) && !a.getDate().isAfter(endOfMonth))
                .count();
            
            String monthName = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            trends.add(new MonthlyServiceTrend(monthName, (int) count));
        }
        
        return trends;
    }
    
    private List<EmployeeWorkload> getEmployeeWorkload(List<Appointment> appointments) {
        // Group appointments by employee and count
        Map<String, Long> employeeCounts = appointments.stream()
            .filter(a -> a.getEmployee() != null)
            .filter(a -> a.getStatus() == AppointmentStatus.IN_PROGRESS || 
                        a.getStatus() == AppointmentStatus.APPROVE)
            .collect(Collectors.groupingBy(
                a -> a.getEmployee().getFullName(),
                Collectors.counting()
            ));
        
        // Sort by count descending and take top employees
        return employeeCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(4)
            .map(entry -> new EmployeeWorkload(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    private List<UpcomingAppointment> getUpcomingAppointments() {
        LocalDate today = LocalDate.now();
        
        return appointmentRepository.findAll().stream()
            .filter(a -> !a.getDate().isBefore(today))
            .filter(a -> a.getStatus() != AppointmentStatus.REJECT && 
                        a.getStatus() != AppointmentStatus.COMPLETED)
            .sorted(Comparator.comparing(Appointment::getDate)
                              .thenComparing(Appointment::getTime))
            .limit(5)
            .map(appointment -> new UpcomingAppointment(
                appointment.getCustomer() != null ? appointment.getCustomer().getFullName() : "Unknown",
                appointment.getVehicleType(),
                appointment.getService(),
                appointment.getDate()
            ))
            .collect(Collectors.toList());
    }
}