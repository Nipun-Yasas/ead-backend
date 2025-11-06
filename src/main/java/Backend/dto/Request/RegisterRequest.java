package Backend.dto.Request;

public record RegisterRequest(

    String fullName,
    String email,
    String phone,
    String password

){}
