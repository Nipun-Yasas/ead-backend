package Backend.service;

import Backend.dto.Request.CreateUserRequest;
import Backend.dto.Request.LoginRequest;
import Backend.dto.Request.RegisterRequest;
import Backend.dto.Response.AuthResponse;
import Backend.dto.Response.UserResponse;
import Backend.entity.Role;
import Backend.entity.Role.RoleName;
import Backend.entity.User;
import Backend.repository.RoleRepository;
import Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        user.setRole(customerRole);

        userRepository.save(user);

        String JWT = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));

        return new AuthResponse(
                user.getName(),
                user.getEmail(),
                user.getRole().getName().name(),
                JWT
        );
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String JWT = jwtService.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));

        return new AuthResponse(
                user.getName(),
                user.getEmail(),
                user.getRole().getName().name(),
                JWT
        );
    }

    @Transactional
    public UserResponse createAdmin(CreateUserRequest request) {
        return createUserWithRole(request, RoleName.ADMIN);
    }

    @Transactional
    public UserResponse createEmployee(CreateUserRequest request) {
        return createUserWithRole(request, RoleName.EMPLOYEE);
    }

    private UserResponse createUserWithRole(CreateUserRequest request, RoleName roleName) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(roleName + " role not found"));
        user.setRole(role);

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getName() == RoleName.SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete super admin");
        }

        userRepository.delete(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.isEnabled(),
                user.getRole().getName().name()
        );
    }
}