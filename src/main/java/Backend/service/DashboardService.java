package Backend.service;

import Backend.dto.Response.*;
import Backend.entity.Appointment;
import Backend.entity.Service.ServiceStatus;
import Backend.repository.AppointmentRepository;
import Backend.repository.ServiceRepository;
import Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    
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
                appointment.getCustomer() != null ? appointment.getCustomer().getFullName() : "Unknown",
                appointment.getVehicleType(),
                appointment.getService(),
                appointment.getDate()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get comprehensive dashboard statistics based on appointments
     */
    public DashboardStatsResponse getAppointmentDashboardStats() {
        // Get all appointments
        List<Appointment> allAppointments = appointmentRepository.findAll();
        
        // Calculate total counts by status
        Long totalServices = (long) allAppointments.size();
        Long completedServices = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
            .count();
        Long inProgressServices = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS)
            .count();
        Long pendingServices = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING)
            .count();
        Long cancelledServices = allAppointments.stream()
            .filter(a -> a.getStatus() == Appointment.AppointmentStatus.REJECT)
            .count();
        Long todayAppointments = allAppointments.stream()
            .filter(a -> a.getDate().equals(LocalDate.now()))
            .count();
        
        // Services by status
        List<DashboardStatsResponse.StatusCount> servicesByStatus = Arrays.stream(Appointment.AppointmentStatus.values())
            .map(status -> {
                long count = allAppointments.stream()
                    .filter(a -> a.getStatus() == status)
                    .count();
                return new DashboardStatsResponse.StatusCount(status.name(), count);
            })
            .filter(sc -> sc.getCount() > 0)
            .collect(Collectors.toList());
        
        // Monthly trend (last 12 months)
        List<DashboardStatsResponse.MonthlyTrend> monthlyTrend = getMonthlyAppointmentTrend(allAppointments);
        
        // Employee workload (top employees by task count)
        List<DashboardStatsResponse.EmployeeWorkload> employeeWorkload = getEmployeeWorkloadStats();
        
        // Upcoming appointments (next 5 appointments)
        List<DashboardStatsResponse.UpcomingAppointment> upcomingAppointments = getUpcomingAppointmentsList();
        
        return new DashboardStatsResponse(
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
    
    private List<DashboardStatsResponse.MonthlyTrend> getMonthlyAppointmentTrend(List<Appointment> allAppointments) {
        LocalDate now = LocalDate.now();
        List<DashboardStatsResponse.MonthlyTrend> trends = new ArrayList<>();
        
        // Get last 12 months
        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            long count = allAppointments.stream()
                .filter(a -> a.getDate().getYear() == monthDate.getYear() && 
                            a.getDate().getMonth() == monthDate.getMonth())
                .count();
            
            trends.add(new DashboardStatsResponse.MonthlyTrend(monthName, count));
        }
        
        return trends;
    }
    
    private List<DashboardStatsResponse.EmployeeWorkload> getEmployeeWorkloadStats() {
        // Get all appointments with employees
        List<Appointment> appointmentsWithEmployees = appointmentRepository.findAll().stream()
            .filter(a -> a.getEmployee() != null)
            .collect(Collectors.toList());
        
        // Group by employee and count
        Map<String, Long> employeeCounts = appointmentsWithEmployees.stream()
            .collect(Collectors.groupingBy(
                a -> a.getEmployee().getFullName(),
                Collectors.counting()
            ));
        
        // Convert to list and sort by count descending
        return employeeCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(4) // Top 4 employees
            .map(entry -> new DashboardStatsResponse.EmployeeWorkload(
                entry.getKey(),
                entry.getValue()
            ))
            .collect(Collectors.toList());
    }
    
    private List<DashboardStatsResponse.UpcomingAppointment> getUpcomingAppointmentsList() {
        LocalDate today = LocalDate.now();
        
        return appointmentRepository.findAll().stream()
            .filter(a -> !a.getDate().isBefore(today)) // Future or today
            .filter(a -> a.getStatus() != Appointment.AppointmentStatus.REJECT) // Not cancelled
            .filter(a -> a.getStatus() != Appointment.AppointmentStatus.COMPLETED) // Not completed
            .sorted(Comparator.comparing(Appointment::getDate)
                .thenComparing(Appointment::getTime))
            .limit(5) // Next 5 appointments
            .map(a -> new DashboardStatsResponse.UpcomingAppointment(
                a.getCustomer() != null ? a.getCustomer().getFullName() : "Unknown",
                a.getVehicleType(),
                a.getService(),
                a.getDate().toString() + "T" + a.getTime().toString()
            ))
            .collect(Collectors.toList());
    }
}