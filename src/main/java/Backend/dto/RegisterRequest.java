package Backend.dto;

public record RegisterRequest(

    String email,
    String name,
    String password

){}
