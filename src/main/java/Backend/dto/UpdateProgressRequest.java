package Backend.dto;

import lombok.Data;

@Data
public class UpdateProgressRequest {
    private String progress; // e.g., "PENDING", "IN_PROGRESS", "COMPLETED"
}