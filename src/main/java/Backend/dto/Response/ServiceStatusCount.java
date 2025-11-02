package Backend.dto.Response;

public record ServiceStatusCount(
    String status,
    Long count
) {}