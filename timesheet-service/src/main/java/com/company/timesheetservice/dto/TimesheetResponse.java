package com.company.timesheetservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class TimesheetResponse {

    private Long id;
    private Long userId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String status;
    private Double totalHours;
    private LocalDateTime submittedAt;
    private String reviewComment;
    private List<TimesheetEntryResponse> entries;
}