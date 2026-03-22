package com.company.timesheetservice.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.timesheetservice.dto.ProjectResponse;
import com.company.timesheetservice.dto.ReviewRequest;
import com.company.timesheetservice.dto.TimesheetEntryRequest;
import com.company.timesheetservice.dto.TimesheetEntryResponse;
import com.company.timesheetservice.dto.TimesheetResponse;
import com.company.timesheetservice.entity.Project;
import com.company.timesheetservice.entity.Timesheet;
import com.company.timesheetservice.entity.TimesheetEntry;
import com.company.timesheetservice.repository.ProjectRepository;
import com.company.timesheetservice.repository.TimesheetEntryRepository;
import com.company.timesheetservice.repository.TimesheetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimesheetService {
	
	private final TimesheetRepository timesheetRepository;
	private final TimesheetEntryRepository entryRepository;
	private final ProjectRepository projectRepository;
	
	// Maximum hours allowed per day
    private static final double MAX_DAILY_HOURS = 12.0;
    
    public List<ProjectResponse> getAllActiveProjects(){
    	
    	return projectRepository.findByIsActiveTrue().stream()
    			.map(this::mapToProjectResponse)
    			.collect(Collectors.toList());
    }
    
    public TimesheetEntryResponse logEntry(Long userId, TimesheetEntryRequest request) {
    	
    	//Cannot log entry for future dates
    	if(request.getWorkDate().isAfter(LocalDate.now())) {
    		throw new RuntimeException("Cannot log hours for future dates");
    	}
    	
    	//validate project exists
    	Project project = projectRepository.findById(request.getProjectId())
    			.orElseThrow(() -> new RuntimeException("Project not found"));
    	
    	
    	if(!project.getIsActive()) {
    		throw new RuntimeException("Project is inactive");
    	}
    	
    	//get or create timesheet for this week
    	LocalDate weekStart = getWeekStart(request.getWorkDate());
    	LocalDate weekEnd = weekStart.plusDays(6);
    	
    	Timesheet timesheet = timesheetRepository.findByUserIdAndWeekStart(userId, weekStart)
    			.orElseGet(() -> createNewTimesheet(
                        userId, weekStart, weekEnd));
    	
    	// RULE 4: Cannot add entry to submitted/approved timesheet
        if ("SUBMITTED".equals(timesheet.getStatus())
                || "APPROVED".equals(timesheet.getStatus())) {
            throw new RuntimeException(
                "Cannot modify a " + timesheet.getStatus()
                + " timesheet");
        }
        
        //check duplicate entries
        
        if(entryRepository.existsByTimesheetIdAndProjectIdAndWorkDate(timesheet.getId(), project.getId(), request.getWorkDate())) {
        	
        	throw new RuntimeException("Entry already exists for this project and date");
        }
        
        //check daily hours limit
        Double existingHours = entryRepository.findByTimesheetId(timesheet.getId())
        		.stream()
        		.filter(e -> e.getWorkDate().equals(request.getWorkDate()))
        		.mapToDouble(TimesheetEntry::getHoursLogged)
        		.sum();
        
        if (existingHours + request.getHoursLogged() > MAX_DAILY_HOURS) {
        	throw new RuntimeException(
        			"Total hours for " + request.getWorkDate() + " would exceed " + MAX_DAILY_HOURS + " hours");
        }
        
        //save entry
        TimesheetEntry entry = TimesheetEntry.builder()
        						.timesheet(timesheet)
        						.project(project)
        						.workDate(request.getWorkDate())
        						.hoursLogged(request.getHoursLogged())
        						.taskSummary(request.getTaskSummary())
        						.build();
        
        TimesheetEntry savedEntry = entryRepository.save(entry);
        
        updateTotalHours(timesheet);
        
        return mapToEntryResponse(savedEntry);
        
    }
    
    //get weekly timesheet
    public TimesheetResponse getWeeklyTimesheet(Long userId, LocalDate weekStart) {
    	
    	Timesheet timesheet = timesheetRepository.findByUserIdAndWeekStart(userId, weekStart)
    							.orElseThrow(() -> new  RuntimeException("No timesheet found for week: "
    																	+ weekStart));
    	
    	return mapToTimesheetResponse(timesheet);
    }
    
    //get all timesheets
    public List<TimesheetResponse> getAllTimesheet(Long userId){
    	
    	return timesheetRepository.findByUserIdOrderByWeekStartDesc(userId)
    			.stream()
    			.map(this::mapToTimesheetResponse)
    			.collect(Collectors.toList());
    }
    
    //submit timesheet
    @Transactional
    public TimesheetResponse submitTimesheet(Long userId, LocalDate weekStart) {
    	
    	Timesheet timesheet = timesheetRepository.findByUserIdAndWeekStart(userId, weekStart)
    							.orElseThrow(() -> new RuntimeException("No timesheet found for week: "
    																		+weekStart));
    	
    	//Only DRAFT timesheets can be submitted
    	if(!"DRAFT".equals(timesheet.getStatus())) {
    		throw new RuntimeException(
    				"Only DRAFT timesheets can be submitted. Current status: "+timesheet.getStatus());
    	}
    	
    	//Must have at least one entry
    	List<TimesheetEntry> entries = entryRepository.findByTimesheetId(timesheet.getId());
    	
    	if(entries.isEmpty()) {
    		throw new RuntimeException("Cannot submit empty timesheet");
    	}
    	
    	timesheet.setStatus("SUBMITTED");
    	timesheet.setSubmittedAt(LocalDateTime.now());
    	timesheetRepository.save(timesheet);
    	
    	return mapToTimesheetResponse(timesheet);
    }
    
    //Manager review
    @Transactional
    public TimesheetResponse reviewTimesheet(Long timesheetId, Long managerId, ReviewRequest request) {
    	
    	Timesheet timesheet = timesheetRepository.findById(timesheetId)
    							.orElseThrow(() -> 
    							new RuntimeException("Timesheet not found"));
    	
    	//Only SUBMITTED timesheets can be reviewed
    	if(!"SUBMITTED".equals(timesheet.getStatus())) {
    		throw new RuntimeException(
    				"Only SUBMITTED timesheets can be reviewed");
    	}
    	
    	// ✅ Comment mandatory for rejection
        if ("REJECTED".equals(request.getAction())
                && (request.getComment() == null
                    || request.getComment().isBlank())) {
            throw new RuntimeException(
                "Comment is mandatory when rejecting");
        }
        
        timesheet.setStatus(request.getAction());
        timesheet.setReviewedBy(managerId);
        timesheet.setReviewComment(request.getComment());
        timesheet.setReviewedAt(LocalDateTime.now());
        
        if("REJECTED".equals(request.getAction())) {
        	timesheet.setStatus("DRAFT");
        }
        
        timesheetRepository.save(timesheet);
        
        return mapToTimesheetResponse(timesheet);
    }
    
    //Get pending timsheets(manager)
    public List<TimesheetResponse> getPendingTimesheets(){
    	
    	return timesheetRepository.findByStatus("SUBMITTED")
    			.stream()
    			.map(this::mapToTimesheetResponse)
    			.collect(Collectors.toList());
    }

    // ─── Private Helpers ───────────────────────────────

    private Timesheet createNewTimesheet(Long userId, LocalDate weekStart, LocalDate weekEnd) {
        Timesheet timesheet = Timesheet.builder()
                .userId(userId)
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .status("DRAFT")
                .totalHours(0.0)
                .build();
        return timesheetRepository.save(timesheet);
    }

    private void updateTotalHours(Timesheet timesheet) {
        double total = entryRepository
                .findByTimesheetId(timesheet.getId())
                .stream()
                .mapToDouble(TimesheetEntry::getHoursLogged)
                .sum();
        timesheet.setTotalHours(total);
        timesheetRepository.save(timesheet);
    }
    
    private LocalDate getWeekStart(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
    
 // ─── Mappers ───────────────────────────────────────

    private TimesheetResponse mapToTimesheetResponse(Timesheet t) {
    	
        List<TimesheetEntryResponse> entries =
                entryRepository
                        .findByTimesheetId(t.getId())
                        .stream()
                        .map(this::mapToEntryResponse)
                        .collect(Collectors.toList());

        return TimesheetResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .weekStart(t.getWeekStart())
                .weekEnd(t.getWeekEnd())
                .status(t.getStatus())
                .totalHours(t.getTotalHours())
                .submittedAt(t.getSubmittedAt())
                .reviewComment(t.getReviewComment())
                .entries(entries)
                .build();
    }

    private TimesheetEntryResponse mapToEntryResponse(
            TimesheetEntry e) {
        return TimesheetEntryResponse.builder()
                .id(e.getId())
                .projectId(e.getProject().getId())
                .projectName(e.getProject().getProjectName())
                .workDate(e.getWorkDate())
                .hoursLogged(e.getHoursLogged())
                .taskSummary(e.getTaskSummary())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private ProjectResponse mapToProjectResponse(
            Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .projectCode(p.getProjectCode())
                .projectName(p.getProjectName())
                .isActive(p.getIsActive())
                .build();
    }
}
