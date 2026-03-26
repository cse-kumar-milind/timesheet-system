package com.company.timesheetservice.test;

import com.company.timesheetservice.client.AuthServiceClient;
import com.company.timesheetservice.dto.*;
import com.company.timesheetservice.entity.Project;
import com.company.timesheetservice.entity.Timesheet;
import com.company.timesheetservice.entity.TimesheetEntry;
import com.company.timesheetservice.repository.ProjectRepository;
import com.company.timesheetservice.repository.TimesheetEntryRepository;
import com.company.timesheetservice.repository.TimesheetRepository;
import com.company.timesheetservice.service.TimesheetService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Timesheet Service Tests")
class TimesheetServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private TimesheetEntryRepository entryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private TimesheetService timesheetService;

    private Project mockProject;
    private Timesheet mockTimesheet;
    private TimesheetEntry mockEntry;
    private TimesheetEntryRequest entryRequest;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockProject = Project.builder()
                .id(1L)
                .projectCode("PROJ001")
                .projectName("Test Project")
                .isActive(true)
                .build();

        mockTimesheet = Timesheet.builder()
                .id(1L)
                .userId(1L)
                .weekStart(LocalDate.of(2026, 3, 16))
                .weekEnd(LocalDate.of(2026, 3, 22))
                .status("DRAFT")
                .totalHours(0.0)
                .build();

        mockEntry = TimesheetEntry.builder()
                .id(1L)
                .timesheet(mockTimesheet)
                .project(mockProject)
                .workDate(LocalDate.of(2026, 3, 17))
                .hoursLogged(8.0)
                .taskSummary("Working on feature")
                .createdAt(LocalDateTime.now())
                .build();

        entryRequest = new TimesheetEntryRequest();
        entryRequest.setProjectId(1L);
        entryRequest.setWorkDate(LocalDate.of(2026, 3, 17));
        entryRequest.setHoursLogged(8.0);
        entryRequest.setTaskSummary("Working on feature");

        mockUserResponse = UserResponse.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .role("EMPLOYEE")
                .build();
    }

    // ═══════════════════════════════════════════════
    // LOG ENTRY TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Log Entry Tests")
    class LogEntryTests {

        @Test
        @DisplayName("Should log entry successfully")
        void logEntry_Success() {
            when(projectRepository.findById(1L))
                    .thenReturn(Optional.of(mockProject));
            when(timesheetRepository
                    .findByUserIdAndWeekStart(anyLong(),
                            any(LocalDate.class)))
                    .thenReturn(Optional.of(mockTimesheet));
            when(entryRepository
                    .existsByTimesheetIdAndProjectIdAndWorkDate(
                            anyLong(), anyLong(),
                            any(LocalDate.class)))
                    .thenReturn(false);
            when(entryRepository.findByTimesheetId(
                    anyLong()))
                    .thenReturn(new ArrayList<>());
            when(entryRepository.save(any(TimesheetEntry.class)))
                    .thenReturn(mockEntry);
            when(timesheetRepository.save(
                    any(Timesheet.class)))
                    .thenReturn(mockTimesheet);

            TimesheetEntryResponse response =
                timesheetService.logEntry(1L, entryRequest);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals(8.0, response.getHoursLogged());
            verify(entryRepository, times(1))
                    .save(any(TimesheetEntry.class));
        }

        @Test
        @DisplayName("Should throw for future date")
        void logEntry_FutureDate() {
            entryRequest.setWorkDate(
                LocalDate.now().plusDays(1));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.logEntry(
                        1L, entryRequest));

            assertEquals(
                "Cannot log hours for future dates",
                exception.getMessage());

            verify(entryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when project not found")
        void logEntry_ProjectNotFound() {
            when(projectRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                () -> timesheetService.logEntry(
                    1L, entryRequest));
        }

        @Test
        @DisplayName("Should throw when project is inactive")
        void logEntry_InactiveProject() {
            mockProject.setIsActive(false);
            when(projectRepository.findById(1L))
                    .thenReturn(Optional.of(mockProject));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.logEntry(
                        1L, entryRequest));

            assertEquals("Project is inactive",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw for duplicate entry")
        void logEntry_DuplicateEntry() {
            when(projectRepository.findById(1L))
                    .thenReturn(Optional.of(mockProject));
            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.of(mockTimesheet));
            when(entryRepository
                    .existsByTimesheetIdAndProjectIdAndWorkDate(
                            anyLong(), anyLong(), any()))
                    .thenReturn(true); // duplicate!

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.logEntry(
                        1L, entryRequest));

            assertTrue(exception.getMessage()
                .contains("Entry already exists"));
        }

        @Test
        @DisplayName("Should throw when daily hours exceed limit")
        void logEntry_ExceedsDailyLimit() {
            // Already have 10 hours logged today
            TimesheetEntry existingEntry =
                TimesheetEntry.builder()
                    .workDate(LocalDate.of(2026, 3, 17))
                    .hoursLogged(10.0)
                    .project(mockProject)
                    .build();

            when(projectRepository.findById(1L))
                    .thenReturn(Optional.of(mockProject));
            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.of(mockTimesheet));
            when(entryRepository
                    .existsByTimesheetIdAndProjectIdAndWorkDate(
                            anyLong(), anyLong(), any()))
                    .thenReturn(false);
            when(entryRepository.findByTimesheetId(
                    anyLong()))
                    .thenReturn(List.of(existingEntry));

            // Trying to add 5 more hours (10+5=15 > 12)
            entryRequest.setHoursLogged(5.0);

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.logEntry(
                        1L, entryRequest));

            assertTrue(exception.getMessage()
                .contains("exceed"));
        }

        @Test
        @DisplayName("Should throw when modifying submitted timesheet")
        void logEntry_SubmittedTimesheet() {
            mockTimesheet.setStatus("SUBMITTED");

            when(projectRepository.findById(1L))
                    .thenReturn(Optional.of(mockProject));
            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.of(mockTimesheet));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.logEntry(
                        1L, entryRequest));

            assertTrue(exception.getMessage()
                .contains("Cannot modify a SUBMITTED"));
        }
    }

    // ═══════════════════════════════════════════════
    // SUBMIT TIMESHEET TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Submit Timesheet Tests")
    class SubmitTimesheetTests {

        @Test
        @DisplayName("Should submit timesheet successfully")
        void submitTimesheet_Success() {
            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.of(mockTimesheet));
            when(entryRepository.findByTimesheetId(
                    anyLong()))
                    .thenReturn(List.of(mockEntry));
            when(timesheetRepository.save(
                    any(Timesheet.class)))
                    .thenReturn(mockTimesheet);
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            TimesheetResponse response =
                timesheetService.submitTimesheet(
                    1L,
                    LocalDate.of(2026, 3, 16));

            assertNotNull(response);
            verify(timesheetRepository, times(1))
                    .save(any(Timesheet.class));
        }

        @Test
        @DisplayName("Should throw when timesheet already submitted")
        void submitTimesheet_AlreadySubmitted() {
            mockTimesheet.setStatus("SUBMITTED");

            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.of(mockTimesheet));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.submitTimesheet(
                        1L,
                        LocalDate.of(2026, 3, 16)));

            assertTrue(exception.getMessage()
                .contains("Only DRAFT timesheets"));
        }

        @Test
        @DisplayName("Should throw when timesheet is empty")
        void submitTimesheet_EmptyTimesheet() {
            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.of(mockTimesheet));
            when(entryRepository.findByTimesheetId(
                    anyLong()))
                    .thenReturn(new ArrayList<>()); // empty!

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.submitTimesheet(
                        1L,
                        LocalDate.of(2026, 3, 16)));

            assertEquals("Cannot submit empty timesheet",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw when timesheet not found")
        void submitTimesheet_NotFound() {
            when(timesheetRepository
                    .findByUserIdAndWeekStart(
                            anyLong(), any()))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                () -> timesheetService.submitTimesheet(
                    1L,
                    LocalDate.of(2026, 3, 16)));
        }
    }

    // ═══════════════════════════════════════════════
    // REVIEW TIMESHEET TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Review Timesheet Tests")
    class ReviewTimesheetTests {

        private ReviewRequest approveRequest;
        private ReviewRequest rejectRequest;

        @BeforeEach
        void setUp() {
            mockTimesheet.setStatus("SUBMITTED");

            approveRequest = new ReviewRequest();
            approveRequest.setAction("APPROVED");
            approveRequest.setComment("Good work!");

            rejectRequest = new ReviewRequest();
            rejectRequest.setAction("REJECTED");
            rejectRequest.setComment("Please fix issues");
        }

        @Test
        @DisplayName("Should approve timesheet successfully")
        void reviewTimesheet_Approve() {
            when(timesheetRepository.findById(1L))
                    .thenReturn(Optional.of(mockTimesheet));
            when(timesheetRepository.save(
                    any(Timesheet.class)))
                    .thenReturn(mockTimesheet);
            when(entryRepository.findByTimesheetId(
                    anyLong()))
                    .thenReturn(List.of(mockEntry));
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            TimesheetResponse response =
                timesheetService.reviewTimesheet(
                    1L, 2L, approveRequest);

            assertNotNull(response);
            verify(timesheetRepository, times(1))
                    .save(any(Timesheet.class));
        }

        @Test
        @DisplayName("Should reject timesheet and set back to DRAFT")
        void reviewTimesheet_Reject() {
            when(timesheetRepository.findById(1L))
                    .thenReturn(Optional.of(mockTimesheet));
            when(timesheetRepository.save(
                    any(Timesheet.class)))
                    .thenAnswer(inv -> {
                        Timesheet t = inv.getArgument(0);
                        // After rejection → status = DRAFT
                        assertEquals("DRAFT", t.getStatus());
                        return t;
                    });
            when(entryRepository.findByTimesheetId(
                    anyLong()))
                    .thenReturn(List.of(mockEntry));
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            timesheetService.reviewTimesheet(
                1L, 2L, rejectRequest);

            verify(timesheetRepository, times(1))
                    .save(any(Timesheet.class));
        }

        @Test
        @DisplayName("Should throw when rejecting without comment")
        void reviewTimesheet_RejectWithoutComment() {
            rejectRequest.setComment(null);

            when(timesheetRepository.findById(1L))
                    .thenReturn(Optional.of(mockTimesheet));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.reviewTimesheet(
                        1L, 2L, rejectRequest));

            assertEquals(
                "Comment is mandatory when rejecting",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw when reviewing non-submitted timesheet")
        void reviewTimesheet_NotSubmitted() {
            mockTimesheet.setStatus("DRAFT");

            when(timesheetRepository.findById(1L))
                    .thenReturn(Optional.of(mockTimesheet));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> timesheetService.reviewTimesheet(
                        1L, 2L, approveRequest));

            assertTrue(exception.getMessage()
                .contains("Only SUBMITTED"));
        }
    }

    // ═══════════════════════════════════════════════
    // GET PROJECTS TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Get Projects Tests")
    class GetProjectsTests {

        @Test
        @DisplayName("Should return all active projects")
        void getAllActiveProjects_Success() {
            Project project2 = Project.builder()
                    .id(2L)
                    .projectCode("PROJ002")
                    .projectName("Leave System")
                    .isActive(true)
                    .build();

            when(projectRepository.findByIsActiveTrue())
                    .thenReturn(List.of(
                        mockProject, project2));

            List<ProjectResponse> projects =
                timesheetService.getAllActiveProjects();

            assertNotNull(projects);
            assertEquals(2, projects.size());
            assertTrue(projects.stream()
                .allMatch(p -> p.getIsActive()));
        }

        @Test
        @DisplayName("Should return empty when no active projects")
        void getAllActiveProjects_Empty() {
            when(projectRepository.findByIsActiveTrue())
                    .thenReturn(new ArrayList<>());

            List<ProjectResponse> projects =
                timesheetService.getAllActiveProjects();

            assertTrue(projects.isEmpty());
        }
    }
}