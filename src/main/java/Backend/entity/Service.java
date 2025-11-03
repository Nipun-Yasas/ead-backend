package Backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String serviceType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceStatus status;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;
    
    private String vehicleModel;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime completedAt;
    
    public enum ServiceStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}