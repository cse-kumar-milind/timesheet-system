package com.company.leaveservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LeaveBalanceDto {

    private Long id;
    private String leaveType;
    private Double totalDays;
    private Double usedDays;
    private Double remainingDays;
    private Integer year;
}