package Backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Backend.entity.Appointment;
import Backend.entity.Role;
import Backend.entity.User;
import Backend.repository.AppointmentRepository;
import Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    /**
     * Find all users by role name
     * @param roleName - "EMPLOYEE", "ADMIN", "SUPER_ADMIN", or "CUSTOMER"
     * @return List of users with the specified role
     */
    @Transactional(readOnly = true)
    public List<User> findByRoleName(String roleName) {
        try {
            // Convert String to RoleName enum
            Role.RoleName roleNameEnum = Role.RoleName.valueOf(roleName.toUpperCase());
            return userRepository.findByRole_Name(roleNameEnum);
        } catch (IllegalArgumentException e) {
            // If invalid role name, return empty list
            throw new IllegalArgumentException("Invalid role name: " + roleName + 
                ". Valid roles are: SUPER_ADMIN, ADMIN, EMPLOYEE, CUSTOMER");
        }
    }

    /**
     * Get task statistics for an employee
     * Returns count of appointments by status
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getEmployeeTaskStats(User employee) {
        Map<String, Long> stats = new HashMap<>();
        
        // Count appointments for each status
        stats.put("pending", appointmentRepository.countByEmployeeAndStatus(employee, Appointment.AppointmentStatus.PENDING));
        stats.put("approved", appointmentRepository.countByEmployeeAndStatus(employee, Appointment.AppointmentStatus.APPROVE));
        stats.put("inProgress", appointmentRepository.countByEmployeeAndStatus(employee, Appointment.AppointmentStatus.IN_PROGRESS));
        stats.put("completed", appointmentRepository.countByEmployeeAndStatus(employee, Appointment.AppointmentStatus.COMPLETED));
        
        // Calculate total tasks
        long total = stats.values().stream().mapToLong(Long::longValue).sum();
        stats.put("total", total);
        
        return stats;
    }
}