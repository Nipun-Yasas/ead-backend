package Backend.repository;

import Backend.entity.Appointment;
import Backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find appointments by customer
    List<Appointment> findByCustomer(User customer);

    // Find appointments by customer with pagination
    Page<Appointment> findByCustomer(User customer, Pageable pageable);

    // Find appointments by employee
    List<Appointment> findByEmployee(User employee);

    // Find appointments by employee with pagination
    Page<Appointment> findByEmployee(User employee, Pageable pageable);

    // Find appointments by status
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    // Find appointments by status with pagination
    Page<Appointment> findByStatus(Appointment.AppointmentStatus status, Pageable pageable);

    // Find appointments by date
    List<Appointment> findByDate(LocalDate date);

    // Find appointments by date range
    @Query("SELECT a FROM Appointment a WHERE a.date BETWEEN :startDate AND :endDate ORDER BY a.date, a.time")
    List<Appointment> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Check if time slot is available
        @Query("SELECT a FROM Appointment a WHERE a.date = :date AND a.time = :time AND a.status != 'REJECT'")
    Optional<Appointment> findByDateAndTimeAndStatusNot(@Param("date") LocalDate date, @Param("time") LocalTime time);

    // Find appointments for a specific customer by status
    List<Appointment> findByCustomerAndStatus(User customer, Appointment.AppointmentStatus status);

    // Find today's appointments
    @Query("SELECT a FROM Appointment a WHERE a.date = CURRENT_DATE ORDER BY a.time")
    List<Appointment> findTodaysAppointments();

    // Find upcoming appointments for a customer
    @Query("SELECT a FROM Appointment a WHERE a.customer = :customer AND a.date >= CURRENT_DATE ORDER BY a.date, a.time")
    List<Appointment> findUpcomingAppointmentsByCustomer(@Param("customer") User customer);

    // Find appointments by vehicle number
    List<Appointment> findByVehicleNumberContainingIgnoreCase(String vehicleNumber);

    // Count appointments by status
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") Appointment.AppointmentStatus status);

    // Find appointments between dates with status
    @Query("SELECT a FROM Appointment a WHERE a.date BETWEEN :startDate AND :endDate AND a.status = :status ORDER BY a.date, a.time")
    List<Appointment> findByDateBetweenAndStatus(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") Appointment.AppointmentStatus status);

    Long countByDate(LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.date >= :startDate " +
            "ORDER BY a.date ASC")
    List<Appointment> findUpcomingAppointments(LocalDate startDate);

}