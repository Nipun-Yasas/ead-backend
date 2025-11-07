package Backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Backend.entity.User;
import Backend.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get all employees with task statistics
     * PUBLIC - No authentication required (for testing)
     * Used for: Task Allocation - employee selection with workload info
     */
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {
        List<User> employees = userService.findByRoleName("EMPLOYEE");
        
        // Map to response DTO (exclude sensitive data, include task stats)
        List<EmployeeResponse> response = employees.stream()
                .filter(User::isEnabled) // Only return enabled employees
                .map(user -> {
                    Map<String, Long> taskStats = userService.getEmployeeTaskStats(user);
                    return new EmployeeResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().getName().name(),
                        user.isEnabled(),
                        taskStats
                    );
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all customers
     * PUBLIC - No authentication required (for testing)
     * Used for: Creating appointments, viewing customer list
     */
    @GetMapping("/customers")
    public ResponseEntity<List<UserResponse>> getCustomers() {
        List<User> customers = userService.findByRoleName("CUSTOMER");
        
        // Map to response DTO (exclude sensitive data)
        List<UserResponse> response = customers.stream()
                .filter(User::isEnabled) // Only return enabled customers
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole().getName().name(),
                    user.isEnabled()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Response DTO for employee information with task statistics
     */
    record EmployeeResponse(
        Long id, 
        String fullName, 
        String email,
        String phone,
        String role, 
        boolean enabled,
        Map<String, Long> taskStats  // Task counts by status
    ) {}

    /**
     * Response DTO for user information
     * Excludes sensitive data like password
     */
    record UserResponse(
        Long id, 
        String fullName, 
        String email,
        String phone,
        String role, 
        boolean enabled
    ) {}
}