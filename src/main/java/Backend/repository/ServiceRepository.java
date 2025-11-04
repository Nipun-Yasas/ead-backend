package Backend.repository;

import Backend.entity.Service;
import Backend.entity.Service.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {


    Long countByStatus(ServiceStatus status);
    
    @Query("SELECT s.status as status, COUNT(s) as count FROM Service s GROUP BY s.status")
    List<Object[]> countByStatusGrouped();
    
    @Query("SELECT FUNCTION('TO_CHAR', s.createdAt, 'Mon') as month, COUNT(s) as value " +
           "FROM Service s WHERE s.createdAt >= :startDate GROUP BY FUNCTION('TO_CHAR', s.createdAt, 'Mon') " +
           "ORDER BY MIN(s.createdAt)")
    List<Object[]> getMonthlyTrend(LocalDateTime startDate);
    
    @Query("SELECT e.fullName as employeeName, COUNT(s) as taskCount " +
           "FROM Service s JOIN s.employee e WHERE s.status != :completedStatus " +
           "GROUP BY e.fullName ORDER BY COUNT(s) DESC")
    List<Object[]> getEmployeeWorkload(ServiceStatus completedStatus);
}