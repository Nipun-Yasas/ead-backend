package Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Backend.entity.Role;
import Backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Find all users by role name enum
     * Spring Data JPA automatically generates: WHERE role.name = ?
     */
    List<User> findByRole_Name(Role.RoleName roleName);
    List<User> findByRole(Role role);
    long countByEnabled(boolean enabled);
}