package com.company.adminservice.controller;

import com.company.adminservice.dto.*;
import com.company.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin dashboard and management APIs")
public class AdminController {

    private final AdminService adminService;

    // ─── Dashboard ─────────────────────────────────────

    // GET http://localhost:8080/admin/dashboard
    @Operation(summary = "Get dashboard", description = "[Manager/Admin] Fetch aggregated dashboard data")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {

        return ResponseEntity.ok(
            adminService.getDashboard(role));
    }

    @Operation(summary = "Get employee dashboard summary", description = "[Employee] Fetch aggregate dashboard data for employee")
    @GetMapping("/dashboard/employee-summary")
    public ResponseEntity<Map<String, Object>> getEmployeeSummary(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email) {
        
        return ResponseEntity.ok(Map.of(
            "pendingActions", 0,
            "nextHoliday", "New Year",
            "message", "Employee summary data retrieved"
        ));
    }

    // ─── Public Config ─────────────────────────────────

    @Operation(summary = "Get public configs", description = "[Public] Fetch static announcements and config")
    @GetMapping("/config/public")
    public ResponseEntity<Map<String, Object>> getPublicConfig() {
        return ResponseEntity.ok(Map.of(
            "status", "online",
            "announcements", List.of("Timesheets due Friday!", "Welcome to the new system."),
            "version", "1.0"
        ));
    }

    // ─── Master Data & Policies ─────────────────────────

    @Operation(summary = "Get all policies", description = "[Admin/HR] Fetch organization master data and policies")
    @GetMapping("/master/policies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPolicies(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(Map.of(
            "policies", List.of("Standard PTO", "Sick Leave")
        ));
    }

    // ─── Reports ────────────────────────────────────────

    @Operation(summary = "Get Timesheet Compliance Report", description = "[Admin] Generate compliance report")
    @GetMapping("/reports/timesheet-compliance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTimesheetCompliance(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(Map.of("reportName", "Timesheet Compliance", "status", "GENERATED"));
    }

    @Operation(summary = "Get Leave Consumption Report", description = "[Admin] Generate leave report")
    @GetMapping("/reports/leave-consumption")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLeaveConsumption(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(Map.of("reportName", "Leave Consumption", "status", "GENERATED"));
    }

    // ─── Users ─────────────────────────────────────────

    @Operation(summary = "Delete user by email", description = "[Admin] Delete a user account using raw email string as request body")
    @DeleteMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserByEmail(@RequestBody String email) {
        return ResponseEntity.ok(adminService.deleteUserByEmail(email));
    }

    // GET http://localhost:8080/admin/users
    @Operation(summary = "Get all users", description = "[Admin] Fetch all registered users")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
            adminService.getAllUsers());
    }

    // GET http://localhost:8080/admin/users/1
    @Operation(summary = "Get user by ID", description = "[Manager/Admin] Fetch a specific user's details")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {

        if (!"ADMIN".equals(role)
                && !"MANAGER".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
            adminService.getUserById(id));
    }

    // ─── Timesheets ────────────────────────────────────

    // GET http://localhost:8080/admin/timesheets/pending
    @Operation(summary = "Get pending timesheets", description = "[Manager/Admin] Fetch all timesheets pending review")
    @GetMapping("/timesheets/pending")
    public ResponseEntity<List<TimesheetResponse>>
            getPendingTimesheets(
                @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {

        if (!"MANAGER".equals(role)
                && !"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
            adminService.getPendingTimesheets(role));
    }

    // ─── Leaves ────────────────────────────────────────

    // GET http://localhost:8080/admin/leaves/pending
    @Operation(summary = "Get pending leaves", description = "[Manager/Admin] Fetch all leave requests pending review")
    @GetMapping("/leaves/pending")
    public ResponseEntity<List<LeaveResponseDto>>
            getPendingLeaves(
                @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {

        if (!"MANAGER".equals(role)
                && !"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
            adminService.getPendingLeaves(role));
    }
}