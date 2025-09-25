package Backend.dto;

public record AuthResponse(

    String accessToken,
    String tokenType,
    long expiresAt

){}
