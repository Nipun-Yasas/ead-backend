package Backend.dto.Response;

public record UserResponse(
    Long id,
    String email,
    String name,
    boolean enabled,
    String roles
) {}