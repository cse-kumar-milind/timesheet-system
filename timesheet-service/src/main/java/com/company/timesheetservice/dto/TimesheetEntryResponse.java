package com.company.timesheetservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TimesheetEntryResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private LocalDate workDate;
    private Double hoursLogged;
    private String taskSummary;
    private LocalDateTime createdAt;
}