package Backend.dto.Request;

public record RegisterRequest(

    String email,
    String name,
    String password

){}
