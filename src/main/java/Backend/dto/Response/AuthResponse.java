package Backend.dto.Response;

public record AuthResponse(
    String username,
    String email,
    String role,
    String JWT
) {}
