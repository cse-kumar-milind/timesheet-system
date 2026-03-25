package com.company.adminservice.dto;

import lombok.*;
import java.util.List;

@Getter 
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Timesheet stats
    private long pendingTimesheets;
    private long approvedTimesheets;
    private long rejectedTimesheets;

    // Leave stats
    private long pendingLeaves;
    private long approvedLeaves;
    private long rejectedLeaves;

    // Lists
    private List<TimesheetResponse> recentTimesheets;
    private List<LeaveResponseDto> recentLeaves;
    private List<UserResponse> allEmployees;
}