package com.company.leaveservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LeaveResponseDto {

    private Long id;
    private Long userId;
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