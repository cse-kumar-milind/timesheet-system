package com.company.adminservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponseDto {
    private Long id;
    private Long userId;
    private String employeeName;
    private String employeeEmail;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Double totalDays;
    private String reason;
    private String status;
    private String managerComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}