package Backend.service;

import Backend.entity.Appointment;
import Backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final AppointmentRepository appointmentRepository;

    public List<Appointment> getAppointmentsByEmployeeId(Long employeeId) {
        List<Appointment> appointments = appointmentRepository.findByEmployeeId(employeeId);
        if (appointments.isEmpty()) {
            throw new RuntimeException("No appointments have been assigned.");
        }
        return appointments;
    }

    public Appointment updateAppointmentProgress(Long appointmentId, Integer progress) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        
        appointment.setProgress(progress);
        
        // Update status to COMPLETED when progress reaches 100
        if (progress != null && progress >= 100) {
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        }
        
        return appointmentRepository.save(appointment);
    }

}