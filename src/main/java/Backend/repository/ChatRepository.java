package Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Backend.entity.Chat;
import Backend.entity.User;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    @Query("SELECT c FROM Chat c WHERE c.customer.id = :customerId ORDER BY c.lastMessageAt DESC")
    List<Chat> findByCustomerOrderByLastMessageDesc(@Param("customerId") Long customerId);
    
    @Query("SELECT c FROM Chat c WHERE c.employee.id = :employeeId ORDER BY c.lastMessageAt DESC")
    List<Chat> findByEmployeeOrderByLastMessageDesc(@Param("employeeId") Long employeeId);
    
    Optional<Chat> findByCustomerAndEmployee(User customer, User employee);
    
    @Query("SELECT DISTINCT c FROM Chat c WHERE (c.customer.id = :userId OR c.employee.id = :userId) ORDER BY c.lastMessageAt DESC")
    List<Chat> findAllUserChats(@Param("userId") Long userId);
}