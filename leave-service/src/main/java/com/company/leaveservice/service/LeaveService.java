package com.company.leaveservice.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.leaveservice.dto.HolidayDto;
import com.company.leaveservice.dto.LeaveBalanceDto;
import com.company.leaveservice.dto.LeaveRequestDto;
import com.company.leaveservice.dto.LeaveResponseDto;
import com.company.leaveservice.dto.LeaveReviewDto;
import com.company.leaveservice.entity.Holiday;
import com.company.leaveservice.entity.LeaveBalance;
import com.company.leaveservice.entity.LeaveRequest;
import com.company.leaveservice.repository.HolidayRepository;
import com.company.leaveservice.repository.LeaveBalanceRepository;
import com.company.leaveservice.repository.LeaveRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveService {
	
	private final LeaveRequestRepository leaveRequestRepository;
	private final LeaveBalanceRepository leaveBalanceRepository;
	private final HolidayRepository holidayRepository;
	
	//Apply for leave
	
	@Transactional
	public LeaveResponseDto applyLeave(Long userId, LeaveRequestDto request) {
		
		//fromDate must be before toDate
		if(request.getFromDate().isAfter(request.getToDate())) {
			
			throw new RuntimeException(
	                "From date cannot be after to date");
		}
		
		//cannot apply for past dates
		if(request.getFromDate().isBefore(LocalDate.now())) {
			throw new RuntimeException(
	                "Cannot apply leave for past dates");
		}
		
		//check for overlapping leave
		
		List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeave(userId,
																		request.getFromDate(), request.getToDate());
		
		if(!overlapping.isEmpty()) {
			throw new RuntimeException(
	                "Leave dates overlap with an existing leave request");
		}
		
		//Calculate working days (excluding weekends and holidays)
		double workingDays = calculateWorkingDays(
							request.getFromDate(),
							request.getToDate());
		
		if(workingDays == 0) {
			throw new RuntimeException(
	                "Selected dates have no working days " +
	                "(weekends/holidays only)");
		}
		
		//check leave balance
		int year = request.getFromDate().getYear();
		
		LeaveBalance balance = leaveBalanceRepository
							   .findByUserIdAndLeaveTypeAndYear(userId, request.getLeaveType(), year)
							   .orElseThrow(() -> new RuntimeException(
									   "No leave balance for "+request.getLeaveType()+" in year "
									   +year+". Please contact HR."));
		
		if(balance.getRemainingDays() < workingDays) {
			throw new RuntimeException(
	                "Insufficient leave balance. " +
	                "Available: " + balance.getRemainingDays() +
	                " days, Required: " + workingDays + " days");
		}
		
		LeaveRequest leaveRequest = LeaveRequest.builder()
									.userId(userId)
									.leaveType(request.getLeaveType())
									.fromDate(request.getFromDate())
									.toDate(request.getToDate())
									.totalDays(workingDays)
									.reason(request.getReason())
									.status("SUBMITTED")
									.build();
		
		return mapToResponse(leaveRequestRepository.save(leaveRequest));
	}
	
	//Get my leave history
	public List<LeaveResponseDto> getMyLeaveHistory(Long userId){
		
		return leaveRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}
	
	//Get My balance
	public List<LeaveBalanceDto> getMyBalances(Long userId){
		
		int currentYear = LocalDate.now().getYear();
		
		return leaveBalanceRepository.findByUserIdAndYear(userId, currentYear)
				.stream()
				.map(this::mapToBalanceDto)
				.collect(Collectors.toList());
	}
	
	//Cancel leave
	@Transactional
	public LeaveResponseDto cancelLeave(Long userId, Long leaveId) {
		
		LeaveRequest leave = leaveRequestRepository.findById(leaveId)
							.orElseThrow(() -> new RuntimeException(
			                        "Leave request not found"));
		
		//Only owner can cancel
		if(!leave.getUserId().equals(userId)) {
			throw new RuntimeException(
			                "You can only cancel your own leave");
		}
		
		//cannot cancel already processed leave
		if("REJECTED".equals(leave.getStatus())
                || "CANCELLED".equals(leave.getStatus())) {
			
			throw new RuntimeException(
	                "Cannot cancel a " +
	                leave.getStatus() + " leave request");
		}
		
		// Cannot cancel past approved leave
        if ("APPROVED".equals(leave.getStatus())
                && leave.getFromDate()
                        .isBefore(LocalDate.now())) {
            throw new RuntimeException(
                "Cannot cancel leave that has " +
                "already started");
        }
        
        leave.setStatus("CANCELLED");
        leaveRequestRepository.save(leave);
        
        return mapToResponse(leave);
		
	}
	
	//Manager review
	@Transactional
	public LeaveResponseDto reviewLeave(Long leaveId, Long managerId, LeaveReviewDto request) {
		
		LeaveRequest leave = leaveRequestRepository
                .findById(leaveId)
                .orElseThrow(() -> new RuntimeException(
                        "Leave request not found"));
		
		//Only SUBMITTED leaves can be reviewed
        if (!"SUBMITTED".equals(leave.getStatus())) {
            throw new RuntimeException(
                "Only SUBMITTED leave can be reviewed. " +
                "Current status: " + leave.getStatus());
        }
        
     // Comment mandatory for rejection
        if ("REJECTED".equals(request.getAction())
                && (request.getComment() == null
                    || request.getComment().isBlank())) {
            throw new RuntimeException(
                "Comment is mandatory when rejecting");
        }
        
        leave.setStatus(request.getAction());
        leave.setManagerId(managerId);
        leave.setManagerComment(request.getComment());
        leave.setReviewedAt(LocalDateTime.now());
        leaveRequestRepository.save(leave);
        

        // ✅ Deduct balance on approval
        if ("APPROVED".equals(request.getAction())) {
            deductLeaveBalance(
                leave.getUserId(),
                leave.getLeaveType(),
                leave.getFromDate().getYear(),
                leave.getTotalDays());
        }
        
        return mapToResponse(leave);
        
	}
	
	//get pending requests
	public List<LeaveResponseDto> getPendingRequests(){
		
		return leaveRequestRepository.findByStatus("SUBMITTED")
				.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}
	
	//Holidays
	public List<Holiday> getHolidays(int year){
		
		return holidayRepository.findByYear(year);
	}
	
	@Transactional
	public Holiday addHoliday(HolidayDto dto) {
		
		if(holidayRepository.existsByHolidayDate(dto.getHolidayDate())) {
			
			throw new RuntimeException(
	                "Holiday already exists for date: " +
	                        dto.getHolidayDate());
		}
		
		Holiday holiday = Holiday.builder()
							.holidayDate(dto.getHolidayDate())
							.holidayName(dto.getHolidayName())
							.holidayType(dto.getHolidayType())
							.build();
		
		return holidayRepository.save(holiday);
	}
	
	//helpers
	
	private double calculateWorkingDays(
            LocalDate from, LocalDate to) {
        double days = 0;
        LocalDate current = from;

        while (!current.isAfter(to)) {
            // Skip weekends
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY
                && current.getDayOfWeek()
                          != DayOfWeek.SUNDAY) {
                // Skip holidays
                if (!holidayRepository
                        .existsByHolidayDate(current)) {
                    days++;
                }
            }
            current = current.plusDays(1);
        }
        return days;
    }
	
	private void deductLeaveBalance(
            Long userId,
            String leaveType,
            int year,
            double days) {
        LeaveBalance balance = leaveBalanceRepository
            .findByUserIdAndLeaveTypeAndYear(
                userId, leaveType, year)
            .orElseThrow(() -> new RuntimeException(
                "Balance not found"));

        balance.setUsedDays(balance.getUsedDays() + days);
        balance.setRemainingDays(
            balance.getTotalDays() - balance.getUsedDays());
        leaveBalanceRepository.save(balance);
    }
	
	 // ─── Mappers ───────────────────────────────────────

    private LeaveResponseDto mapToResponse(
            LeaveRequest l) {
        return LeaveResponseDto.builder()
                .id(l.getId())
                .userId(l.getUserId())
                .leaveType(l.getLeaveType())
                .fromDate(l.getFromDate())
                .toDate(l.getToDate())
                .totalDays(l.getTotalDays())
                .reason(l.getReason())
                .status(l.getStatus())
                .managerComment(l.getManagerComment())
                .reviewedAt(l.getReviewedAt())
                .createdAt(l.getCreatedAt())
                .build();
    }

    private LeaveBalanceDto mapToBalanceDto(
            LeaveBalance b) {
        return LeaveBalanceDto.builder()
                .id(b.getId())
                .leaveType(b.getLeaveType())
                .totalDays(b.getTotalDays())
                .usedDays(b.getUsedDays())
                .remainingDays(b.getRemainingDays())
                .year(b.getYear())
                .build();
    }
}
