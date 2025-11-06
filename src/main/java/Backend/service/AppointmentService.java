package Backend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Backend.dto.Request.CreateAppointmentRequest;
import Backend.dto.Request.UpdateAppointmentRequest;
import Backend.dto.Response.AppointmentResponse;
import Backend.entity.Appointment;
import Backend.entity.User;
import Backend.repository.AppointmentRepository;
import Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Create a new appointment
     */
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        // Check if time slot is available
        if (isTimeSlotTaken(request.getDate(), request.getTime(), null)) {
            throw new RuntimeException("The selected time slot is not available");
        }

        // Create appointment entity
        Appointment appointment = new Appointment();
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setVehicleType(request.getVehicleType());
        appointment.setVehicleNumber(request.getVehicleNumber());
        appointment.setService(request.getService());
        appointment.setInstructions(request.getInstructions());

        // Set customer based on userId if provided, otherwise use authenticated user
        if (request.getUserId() != null) {
            // Use provided userId as customer ID
            User customer = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getUserId()));
            appointment.setCustomer(customer);
        } else {
            // Try to get authenticated user
            try {
                User customer = getCurrentUser();
                appointment.setCustomer(customer);
            } catch (Exception e) {
                // No authenticated user - appointment must have a customer
                throw new RuntimeException("User must be authenticated to create an appointment");
            }
        }

    appointment.setStatus(Appointment.AppointmentStatus.PENDING);

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Send confirmation email
        emailService.sendAppointmentConfirmation(savedAppointment);

        return AppointmentResponse.fromEntity(savedAppointment);
    }

    /**
     * Get all appointments with pagination
     */
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable)
                .map(AppointmentResponse::fromEntity);
    }

    /**
     * Get appointment by ID
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Get appointments for current user
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments() {
        User currentUser = getCurrentUser();

        List<Appointment> appointments = appointmentRepository.findByCustomer(currentUser);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by status
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatus(status);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update appointment
     */
    public AppointmentResponse updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        User currentUser = getCurrentUser();

        // Check if user is the owner or has admin/employee role
        if (!isOwnerOrHasPermission(appointment, currentUser)) {
            throw new RuntimeException("You don't have permission to update this appointment");
        }

        // Update fields if provided
        if (request.getDate() != null) {
            // Check if new time slot is available (excluding current appointment)
            if (!appointment.getDate().equals(request.getDate()) ||
                    !appointment.getTime().equals(request.getTime())) {
                if (isTimeSlotTaken(request.getDate(), request.getTime(), id)) {
                    throw new RuntimeException("The selected time slot is not available");
                }
            }
            appointment.setDate(request.getDate());
        }

        if (request.getTime() != null) {
            appointment.setTime(request.getTime());
        }

        if (request.getVehicleType() != null) {
            appointment.setVehicleType(request.getVehicleType());
        }

        if (request.getVehicleNumber() != null) {
            appointment.setVehicleNumber(request.getVehicleNumber());
        }

        if (request.getService() != null) {
            appointment.setService(request.getService());
        }

        if (request.getInstructions() != null) {
            appointment.setInstructions(request.getInstructions());
        }

        // Track if status changed to APPROVE for email notification
        boolean statusChangedToApprove = false;
        if (request.getStatus() != null) {
            Appointment.AppointmentStatus oldStatus = appointment.getStatus();
            appointment.setStatus(request.getStatus());
            statusChangedToApprove = (oldStatus != Appointment.AppointmentStatus.APPROVE && 
                                     request.getStatus() == Appointment.AppointmentStatus.APPROVE);
        }

        // Only admins/employees can assign employees
        if (request.getEmployeeId() != null && hasAdminOrEmployeeRole(currentUser)) {
            User employee = userRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            appointment.setEmployee(employee);
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        // Send approval email if status changed to APPROVE
        if (statusChangedToApprove) {
            emailService.sendAppointmentApproval(updatedAppointment);
        }
        
        return AppointmentResponse.fromEntity(updatedAppointment);
    }

    /**
     * Cancel appointment
     */
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        User currentUser = getCurrentUser();

        // Check if user is the owner or has admin/employee role
        if (!isOwnerOrHasPermission(appointment, currentUser)) {
            throw new RuntimeException("You don't have permission to cancel this appointment");
        }

    appointment.setStatus(Appointment.AppointmentStatus.REJECT);
        Appointment cancelledAppointment = appointmentRepository.save(appointment);

        return AppointmentResponse.fromEntity(cancelledAppointment);
    }

    /**
     * Delete appointment (admin only)
     */
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        User currentUser = getCurrentUser();

        // Only admins can delete appointments
        if (!hasAdminRole(currentUser)) {
            throw new RuntimeException("Only administrators can delete appointments");
        }

        appointmentRepository.delete(appointment);
    }

    /**
     * Get today's appointments
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTodaysAppointments() {
        List<Appointment> appointments = appointmentRepository.findTodaysAppointments();
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get appointments by date range
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findByDateBetween(startDate, endDate);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
 * Allocate appointment to employee
 * Changes status from CONFIRMED → IN_PROGRESS
 * Used by Task Allocation feature in frontend
 * 
 * @param appointmentId - ID of appointment to allocate
 * @param employeeId - ID of employee to assign
 * @return AppointmentResponse with updated details
 * @throws RuntimeException if appointment or employee not found
 */
public AppointmentResponse allocateToEmployee(Long appointmentId, Long employeeId) {
    // Verify current user has permission
    User currentUser = getCurrentUser();
    if (!hasAdminRole(currentUser)) {
        throw new RuntimeException("Only Super Admin can allocate appointments");
    }
    
    // Find appointment
    Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + appointmentId));
    
    // Validate appointment status
    if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
        throw new RuntimeException(
            "Only CONFIRMED appointments can be allocated. Current status: " + appointment.getStatus()
        );
    }
    
    // Find employee
    User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
    
    // Validate employee role
    String employeeRole = employee.getRole().getName().name();
    if (!"EMPLOYEE".equals(employeeRole)) {
        throw new RuntimeException(
            "Selected user is not an employee. Role: " + employeeRole
        );
    }
    
    // Check if employee is enabled
    if (!employee.isEnabled()) {
        throw new RuntimeException("Employee account is disabled");
    }
    
    // Allocate appointment
    appointment.setEmployee(employee);
    appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
    
    Appointment savedAppointment = appointmentRepository.save(appointment);
    
    // Log allocation for debugging
    System.out.println(
        "✅ Appointment #" + appointmentId + 
        " allocated to employee: " + employee.getFullName() +
        " (ID: " + employeeId + ")"
    );
    
    return AppointmentResponse.fromEntity(savedAppointment);
}

    /**
     * Assign employee to appointment
     */
    public AppointmentResponse assignEmployee(Long appointmentId, Long employeeId) {
        User currentUser = getCurrentUser();

        // Only admins/employees can assign employees
        if (!hasAdminOrEmployeeRole(currentUser)) {
            throw new RuntimeException("You don't have permission to assign employees");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        appointment.setEmployee(employee);
    appointment.setStatus(Appointment.AppointmentStatus.APPROVE);

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        // Send approval email to customer
        emailService.sendAppointmentApproval(updatedAppointment);
        
        return AppointmentResponse.fromEntity(updatedAppointment);
    }

    // Helper methods

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Change appointment status with email notification
     */
    public AppointmentResponse changeAppointmentStatus(Long id, Appointment.AppointmentStatus newStatus, String notes) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        User currentUser = getCurrentUser();

        // Only admins and employees can change status
        if (!hasAdminOrEmployeeRole(currentUser)) {
            throw new RuntimeException("You don't have permission to change appointment status");
        }

        Appointment.AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // Send email notification about status change
        String statusMessage = buildStatusMessage(oldStatus, newStatus, notes);
        emailService.sendStatusChangeEmail(updatedAppointment, statusMessage);

        return AppointmentResponse.fromEntity(updatedAppointment);
    }

    private String buildStatusMessage(Appointment.AppointmentStatus oldStatus, Appointment.AppointmentStatus newStatus, String notes) {
        String baseMessage = switch (newStatus) {
            case PENDING -> "Your appointment is now <strong>pending</strong> review by our team.";
            case APPROVE -> "Great news! Your appointment has been <strong>approved</strong> and is ready to be scheduled.";
            case ACCEPT -> "Your appointment has been <strong>accepted</strong> by our team.";
            case CONFIRMED -> "Your appointment is now <strong>confirmed</strong>. We look forward to serving you!";
            case IN_PROGRESS -> "Your service is now <strong>in progress</strong>. Our technician is working on your vehicle.";
            case ONGOING -> "Your appointment is currently <strong>ongoing</strong>. The work is being performed.";
            case REJECT -> "We regret to inform you that your appointment has been <strong>rejected</strong>. Please contact us for more information or to reschedule.";
        };

        if (notes != null && !notes.trim().isEmpty()) {
            baseMessage += "<br><br><strong>Additional Notes:</strong> " + notes;
        }

        return baseMessage;
    }

    private boolean isTimeSlotTaken(LocalDate date, LocalTime time, Long appointmentId) {
        Optional<Appointment> existingAppointment = appointmentRepository
                .findByDateAndTimeAndStatusNot(date, time);
        return existingAppointment.isPresent() &&
                (appointmentId == null || !existingAppointment.get().getId().equals(appointmentId));
    }

    private boolean isOwnerOrHasPermission(Appointment appointment, User user) {
        return (appointment.getCustomer() != null && appointment.getCustomer().getId().equals(user.getId())) ||
                hasAdminOrEmployeeRole(user);
    }

    private boolean hasAdminOrEmployeeRole(User user) {
        String roleName = user.getRole().getName().name();
        return "SUPER_ADMIN".equals(roleName) ||
                "ADMIN".equals(roleName) ||
                "EMPLOYEE".equals(roleName);
    }

    private boolean hasAdminRole(User user) {
        String roleName = user.getRole().getName().name();
        return "SUPER_ADMIN".equals(roleName) || "ADMIN".equals(roleName);
    }
}