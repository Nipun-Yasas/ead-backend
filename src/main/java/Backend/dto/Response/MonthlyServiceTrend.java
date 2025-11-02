package Backend.dto.Response;

public record MonthlyServiceTrend(
    String month,
    Integer value
) {}