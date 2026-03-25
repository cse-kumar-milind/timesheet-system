package com.company.adminservice.controller;

import com.company.adminservice.dto.*;
import com.company.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ─── Dashboard ─────────────────────────────────────

    // GET http://localhost:8080/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestHeader("X-User-Role") String role) {

        if (!"MANAGER".equals(role)
                && !"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
            adminService.getDashboard(role));
    }

    // ─── Users ─────────────────────────────────────────

    // GET http://localhost:8080/admin/users
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
            adminService.getAllUsers());
    }

    // GET http://localhost:8080/admin/users/1
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader("X-User-Role") String role,
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
    @GetMapping("/timesheets/pending")
    public ResponseEntity<List<TimesheetResponse>>
            getPendingTimesheets(
                @RequestHeader("X-User-Role") String role) {

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
    @GetMapping("/leaves/pending")
    public ResponseEntity<List<LeaveResponseDto>>
            getPendingLeaves(
                @RequestHeader("X-User-Role") String role) {

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