package com.company.timesheetservice.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.timesheetservice.dto.ProjectResponse;
import com.company.timesheetservice.dto.ReviewRequest;
import com.company.timesheetservice.dto.TimesheetEntryRequest;
import com.company.timesheetservice.dto.TimesheetEntryResponse;
import com.company.timesheetservice.dto.TimesheetResponse;
import com.company.timesheetservice.service.TimesheetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/timesheet")
@RequiredArgsConstructor
public class TimesheetController {
	
	private final TimesheetService timesheetService;
	
	//Projects
	
	@GetMapping("/projects")
	public ResponseEntity<List<ProjectResponse>> getActiveProjects(){
		
		return ResponseEntity.ok(timesheetService.getAllActiveProjects());
	}
	
	//LogEntry
	
	@PostMapping("/entries")
	public ResponseEntity<TimesheetEntryResponse> logEntry(@RequestHeader("X-User-Id") Long userId,
															@Valid @RequestBody TimesheetEntryRequest request){
		TimesheetEntryResponse response = timesheetService.logEntry(userId, request);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	//Get Weekly timesheet
	
	@GetMapping("/weeks/{weekStart}")
	public ResponseEntity<TimesheetResponse> getWeeklyTimesheet(@RequestHeader("X-User-Id") Long userId,
																@PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate weekStart){
		return ResponseEntity.ok(timesheetService.getWeeklyTimesheet(userId, weekStart));
	}
	
	//Get All My Timesheets
	
	@GetMapping("/my-timesheets")
	public ResponseEntity<List<TimesheetResponse>> getMyTimesheets(@RequestHeader("X-User-Id") Long userId){
		
		return ResponseEntity.ok(timesheetService.getAllTimesheet(userId));
	}
	
	//Submit Timesheet
	
	@PostMapping("/weeks/{weekStart}/submit")
	public ResponseEntity<TimesheetResponse> submitTimesheet(@RequestHeader("X-User-Id") Long userId, 
															@PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate weekStart){
		
	    
		return ResponseEntity.ok(timesheetService.submitTimesheet(userId, weekStart));
	}
	
	//Manager APIs
	
	@GetMapping("/manager/pending")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public ResponseEntity<List<TimesheetResponse>> getPendingTimesheets(@RequestHeader("X-User-Role") String role){
		
		
		return ResponseEntity.ok(timesheetService.getPendingTimesheets());
	}
	
	@PutMapping("/manager/review/{timesheetId}")
	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
	public ResponseEntity<TimesheetResponse> reviewTimesheet(@RequestHeader("X-User-Id") Long managerId,
															@RequestHeader("X-User-Role") String role,
															@PathVariable Long timesheetId,
															@Valid @RequestBody ReviewRequest request){
		
		
		return ResponseEntity.ok(timesheetService.reviewTimesheet(timesheetId, managerId, request));
	}
}
