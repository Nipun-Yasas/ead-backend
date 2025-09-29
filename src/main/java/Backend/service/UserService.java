package Backend.service;

import Backend.dto.LoginRequest;
import Backend.dto.RegisterRequest;
import Backend.dto.AuthResponse;
import Backend.entity.User;
import Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);

        userRepository.save(user);

        // Generate JWT token here (you'll implement this next)
        return new AuthResponse("jwt-token", "Bearer", System.currentTimeMillis() + 86400000);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT token here (you'll implement this next)
        return new AuthResponse("jwt-token", "Bearer", System.currentTimeMillis() + 86400000);
    }
}