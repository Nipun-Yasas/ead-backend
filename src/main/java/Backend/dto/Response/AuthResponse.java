package Backend.dto.Response;

public record AuthResponse(
    Long id,
    String fullName,
    String email,
    String role,
    String JWT
) {}
