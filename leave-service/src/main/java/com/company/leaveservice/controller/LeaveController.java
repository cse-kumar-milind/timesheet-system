package com.company.leaveservice.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.leaveservice.dto.HolidayDto;
import com.company.leaveservice.dto.LeaveBalanceDto;
import com.company.leaveservice.dto.LeaveRequestDto;
import com.company.leaveservice.dto.LeaveResponseDto;
import com.company.leaveservice.dto.LeaveReviewDto;
import com.company.leaveservice.entity.Holiday;
import com.company.leaveservice.service.LeaveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/leave")
@RequiredArgsConstructor
public class LeaveController {
	
	private final LeaveService leaveService;
	
	//Employee APIs
	
	@PostMapping("/apply")
	public  ResponseEntity<LeaveResponseDto> applyLeave(@RequestHeader("X-User-Id") Long userId,
														@Valid @RequestBody LeaveRequestDto request){
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(leaveService.applyLeave(userId, request));
	}
	
	@GetMapping("/my-requests")
	public ResponseEntity<List<LeaveResponseDto>> getMyRequests(@RequestHeader("X-User-Id") Long userId){
		
		return ResponseEntity.ok(leaveService.getMyLeaveHistory(userId));
	}
	
	@GetMapping("/my-balance")
	public ResponseEntity<List<LeaveBalanceDto>> getMyBalance(@RequestHeader("X-User-Id") Long userId){
		
		return ResponseEntity.ok(leaveService.getMyBalances(userId));
	}
	
	@PutMapping("/cancel/{leaveId}")
	public ResponseEntity<LeaveResponseDto> cancelLeave(@RequestHeader("X-User-Id") Long userId,
														@PathVariable Long leaveId){
		
		return ResponseEntity.ok(leaveService.cancelLeave(userId, leaveId));
	}
	
	//Manager APIs
	
	@GetMapping("/manager/pending")
	public ResponseEntity<List<LeaveResponseDto>> getPendingRequests(@RequestHeader("X-User-Role") String role){
		
		if (!"MANAGER".equals(role)
                && !"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
		
		return ResponseEntity.ok(leaveService.getPendingRequests());
	}
	
	@PutMapping("/manager/review/{leaveId}")
	public ResponseEntity<LeaveResponseDto> reviewLeave(@RequestHeader("X-User-Id") Long managerId,
														@RequestHeader("X-User-Role") String role,
														@PathVariable Long leaveId,
														@Valid @RequestBody LeaveReviewDto request){
		if (!"MANAGER".equals(role)
                && !"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
		
		return ResponseEntity.ok(leaveService.reviewLeave(leaveId, managerId, request));
		
	}
	
	//Holiday APIs
	
	@GetMapping("/holidays")
	public ResponseEntity<List<Holiday>> getHolidays(@RequestParam(defaultValue = "0") int year){
		
		if(year == 0) {
			year = LocalDate.now().getYear();
		}
		
		return ResponseEntity.ok(leaveService.getHolidays(year));
	}
	
	@PostMapping("/holidays")
	public ResponseEntity<Holiday> addHoliday(@RequestHeader("X-User-Role") String role,
												@Valid @RequestBody HolidayDto request){
		
		if (!"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }
		
		return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.addHoliday(request));
	}
	

}
