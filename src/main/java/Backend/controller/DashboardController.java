package Backend.controller;

import Backend.dto.Response.DashboardStatsResponse;
import Backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get comprehensive dashboard statistics
     * Includes:
     * - Total services count by status
     * - Today's appointments
     * - Monthly trend (last 12 months)
     * - Employee workload (top 4 employees)
     * - Upcoming appointments (next 5)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getAppointmentDashboardStats();
        return ResponseEntity.ok(stats);
    }
}