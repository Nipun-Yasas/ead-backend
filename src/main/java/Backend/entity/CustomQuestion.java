package Backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String question;
    
    @Enumerated(EnumType.STRING)
    private QuestionCategory category;
    
    private boolean isActive = true;
    
    public enum QuestionCategory {
        SERVICE_STATUS,
        PICKUP_READY,
        FEEDBACK,
        GENERAL
    }
}