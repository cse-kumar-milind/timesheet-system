package com.company.adminservice.service;

import com.company.adminservice.client.*;
import com.company.adminservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuthServiceClient authServiceClient;
    private final TimesheetServiceClient timesheetServiceClient;
    private final LeaveServiceClient leaveServiceClient;

    // ─── Dashboard ─────────────────────────────────────

    public DashboardResponse getDashboard(String role) {

        // Fetch data from all services
        List<TimesheetResponse> pendingTimesheets =
                timesheetServiceClient
                        .getPendingTimesheets(role);

        List<LeaveResponseDto> pendingLeaves =
                leaveServiceClient
                        .getPendingLeaves(role);

        List<UserResponse> allUsers =
                authServiceClient.getAllUsers();

        // Build dashboard response
        return DashboardResponse.builder()
                .pendingTimesheets(
                        pendingTimesheets.size())
                .approvedTimesheets(0L) // extend later
                .rejectedTimesheets(0L) // extend later
                .pendingLeaves(
                        pendingLeaves.size())
                .approvedLeaves(0L)     // extend later
                .rejectedLeaves(0L)     // extend later
                .recentTimesheets(pendingTimesheets)
                .recentLeaves(pendingLeaves)
                .allEmployees(allUsers)
                .build();
    }

    // ─── Users ─────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return authServiceClient.getAllUsers();
    }

    public String deleteUserByEmail(String email) {
        return authServiceClient.deleteUserByEmail(email);
    }

    public UserResponse getUserById(Long id) {
        return authServiceClient.getUserById(id);
    }

    // ─── Timesheets ────────────────────────────────────

    public List<TimesheetResponse> getPendingTimesheets(
            String role) {
        return timesheetServiceClient
                .getPendingTimesheets(role);
    }

    // ─── Leaves ────────────────────────────────────────

    public List<LeaveResponseDto> getPendingLeaves(
            String role) {
        return leaveServiceClient
                .getPendingLeaves(role);
    }
}