package Backend.controller;

import Backend.dto.UpdateProgressRequest;
import Backend.entity.Appointment;
import Backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/appointments")
@PreAuthorize("hasRole('EMPLOYEE')")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("{employeeId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByEmployeeId(@PathVariable Long employeeId) {
        List<Appointment> appointments = employeeService.getAppointmentsByEmployeeId(employeeId);
        return ResponseEntity.ok(appointments);
    }

    @PatchMapping("{appointmentId}/progress")
    public ResponseEntity<Appointment> updateAppointmentProgress(
            @PathVariable Long appointmentId,
            @RequestBody UpdateProgressRequest request) {
        Appointment updated = employeeService.updateAppointmentProgress(appointmentId, Integer.parseInt(request.getProgress()));
        return ResponseEntity.ok(updated);
    }

}