package Backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Backend.entity.Role;
import Backend.entity.User;
import Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

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
}