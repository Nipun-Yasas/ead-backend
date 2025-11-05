package Backend.service;

import Backend.dto.Request.CreateUserRequest;
import Backend.dto.Response.UserResponse;
import Backend.entity.Role;
import Backend.entity.Role.RoleName;
import Backend.entity.User;
import Backend.repository.RoleRepository;
import Backend.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuperAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Get role
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));

        // Create user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getEmail());

        return mapToUserResponse(savedUser);
    }


    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }


    public List<UserResponse> getUsersByRole(RoleName roleName) {
        log.info("Fetching users by role: {}", roleName);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        return userRepository.findByRole(role).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Prevent deleting super admin
        if (user.getRole().getName() == RoleName.SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete super admin user");
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }


    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        log.info("Toggling user status for ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        // Prevent disabling super admin
        if (user.getRole().getName() == RoleName.SUPER_ADMIN) {
            throw new RuntimeException("Cannot disable super admin user");
        }

        user.setEnabled(!user.isEnabled());
        User updatedUser = userRepository.save(user);
        
        log.info("User status toggled: {} - Enabled: {}", updatedUser.getEmail(), updatedUser.isEnabled());
        return mapToUserResponse(updatedUser);
    }

    public UserStatistics getUserStatistics() {
        
        long totalUsers = userRepository.count();
        long enabledUsers = userRepository.countByEnabled(true);
        long disabledUsers = userRepository.countByEnabled(false);

        return UserStatistics.builder()
                .totalUsers(totalUsers)
                .enabledUsers(enabledUsers)
                .disabledUsers(disabledUsers)
                .adminCount(getUsersByRole(RoleName.ADMIN).size())
                .employeeCount(getUsersByRole(RoleName.EMPLOYEE).size())
                .customerCount(getUsersByRole(RoleName.CUSTOMER).size())
                .build();
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .enabled(user.isEnabled())
                .createdAt(LocalDateTime.now()) // Add this field to User entity if needed
                .updatedAt(LocalDateTime.now()) // Add this field to User entity if needed
                .build();
    }

 
    @Data
    @Builder
    public static class UserStatistics {
        private Long totalUsers;
        private Long enabledUsers;
        private Long disabledUsers;
        private Integer adminCount;
        private Integer employeeCount;
        private Integer customerCount;
    }
}