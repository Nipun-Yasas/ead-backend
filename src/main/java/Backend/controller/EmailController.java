package Backend.controller;

import Backend.dto.Request.SendEmailRequest;
import Backend.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * Send custom email to a recipient
     * Allows admin/employee to send custom messages to customers
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        try {
            emailService.sendCustomEmail(request.getEmail(), request.getMessage());
            return ResponseEntity.ok(new SuccessResponse("Email sent successfully to " + request.getEmail()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to send email: " + e.getMessage()));
        }
    }

    record SuccessResponse(String message) {}
    record ErrorResponse(String message) {}
}
