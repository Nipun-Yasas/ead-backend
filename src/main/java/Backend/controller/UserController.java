package Backend.controller;

import java.util.List;
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
     * Get all employees
     * PUBLIC - No authentication required (for testing)
     * Used for: Task Allocation - employee selection
     */
    @GetMapping("/employees")
    public ResponseEntity<List<UserResponse>> getEmployees() {
        List<User> employees = userService.findByRoleName("EMPLOYEE");
        
        // Map to response DTO (exclude sensitive data)
        List<UserResponse> response = employees.stream()
                .filter(User::isEnabled) // Only return enabled employees
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().getName().name(),
                    user.isEnabled()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Response DTO for user information
     * Excludes sensitive data like password
     */
    record UserResponse(
        Long id, 
        String fullName, 
        String email, 
        String role, 
        boolean enabled
    ) {}
}