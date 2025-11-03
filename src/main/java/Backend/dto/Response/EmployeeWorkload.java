package Backend.dto.Response;

public record EmployeeWorkload(
    String employeeName,
    Long taskCount
) {}