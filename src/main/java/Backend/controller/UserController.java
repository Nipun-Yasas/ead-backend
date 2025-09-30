package Backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Backend.dto.AuthResponse;
import Backend.dto.LoginRequest;
import Backend.dto.RegisterRequest;
import Backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(
        summary = "Register a new user",
        description = "Create a new user account with email, name, and password"
    )
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Registration failed")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        try{
            AuthResponse response=userService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "User login",
        description = "Authenticate user with email and password"
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        try{
            AuthResponse response=userService.login(request);
            return ResponseEntity.ok(response);
        }
        catch(RuntimeException e ){
            return ResponseEntity.badRequest().build();
        }
    }
}