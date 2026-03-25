package com.company.adminservice.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetResponse {
    private Long id;
    private Long userId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String status;
    private Double totalHours;
    private LocalDateTime submittedAt;
    private String reviewComment;
}