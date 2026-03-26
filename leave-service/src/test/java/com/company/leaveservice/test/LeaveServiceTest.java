package com.company.leaveservice.test;

import com.company.leaveservice.client.AuthServiceClient;
import com.company.leaveservice.dto.*;
import com.company.leaveservice.entity.Holiday;
import com.company.leaveservice.entity.LeaveBalance;
import com.company.leaveservice.entity.LeaveRequest;
import com.company.leaveservice.repository.HolidayRepository;
import com.company.leaveservice.repository.LeaveBalanceRepository;
import com.company.leaveservice.repository.LeaveRequestRepository;
import com.company.leaveservice.service.LeaveService;

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
@DisplayName("Leave Service Tests")
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest mockLeaveRequest;
    private LeaveBalance mockLeaveBalance;
    private LeaveRequestDto leaveRequestDto;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockLeaveRequest = LeaveRequest.builder()
                .id(1L)
                .userId(1L)
                .leaveType("CASUAL")
                .fromDate(LocalDate.of(2026, 4, 1))
                .toDate(LocalDate.of(2026, 4, 2))
                .totalDays(2.0)
                .reason("Personal work")
                .status("SUBMITTED")
                .createdAt(LocalDateTime.now())
                .build();

        mockLeaveBalance = LeaveBalance.builder()
                .id(1L)
                .userId(1L)
                .leaveType("CASUAL")
                .totalDays(12.0)
                .usedDays(0.0)
                .remainingDays(12.0)
                .year(2026)
                .build();

        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setLeaveType("CASUAL");
        leaveRequestDto.setFromDate(
            LocalDate.of(2026, 4, 1));
        leaveRequestDto.setToDate(
            LocalDate.of(2026, 4, 2));
        leaveRequestDto.setReason("Personal work");

        mockUserResponse = UserResponse.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .build();
    }

    // ═══════════════════════════════════════════════
    // APPLY LEAVE TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Apply Leave Tests")
    class ApplyLeaveTests {

        @Test
        @DisplayName("Should apply leave successfully")
        void applyLeave_Success() {
            when(leaveRequestRepository
                    .findOverlappingLeave(
                            anyLong(), any(), any()))
                    .thenReturn(new ArrayList<>());
            when(holidayRepository.existsByHolidayDate(
                    any()))
                    .thenReturn(false);
            when(leaveBalanceRepository
                    .findByUserIdAndLeaveTypeAndYear(
                            anyLong(), anyString(), anyInt()))
                    .thenReturn(Optional.of(mockLeaveBalance));
            when(leaveRequestRepository.save(
                    any(LeaveRequest.class)))
                    .thenReturn(mockLeaveRequest);
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            LeaveResponseDto response =
                leaveService.applyLeave(1L, leaveRequestDto);

            assertNotNull(response);
            assertEquals("SUBMITTED", response.getStatus());
            assertEquals("CASUAL", response.getLeaveType());
            verify(leaveRequestRepository, times(1))
                    .save(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("Should throw when fromDate is after toDate")
        void applyLeave_InvalidDateRange() {
            leaveRequestDto.setFromDate(
                LocalDate.of(2026, 4, 5));
            leaveRequestDto.setToDate(
                LocalDate.of(2026, 4, 1));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.applyLeave(
                        1L, leaveRequestDto));

            assertEquals(
                "From date cannot be after to date",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw for past dates")
        void applyLeave_PastDates() {
            leaveRequestDto.setFromDate(
                LocalDate.now().minusDays(5));
            leaveRequestDto.setToDate(
                LocalDate.now().minusDays(1));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.applyLeave(
                        1L, leaveRequestDto));

            assertEquals(
                "Cannot apply leave for past dates",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw when leave overlaps")
        void applyLeave_OverlappingLeave() {
            when(leaveRequestRepository
                    .findOverlappingLeave(
                            anyLong(), any(), any()))
                    .thenReturn(
                        List.of(mockLeaveRequest));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.applyLeave(
                        1L, leaveRequestDto));

            assertTrue(exception.getMessage()
                .contains("overlap"));
        }

        @Test
        @DisplayName("Should throw when insufficient balance")
        void applyLeave_InsufficientBalance() {
            // Only 1 day remaining but requesting 2
            mockLeaveBalance.setRemainingDays(1.0);

            when(leaveRequestRepository
                    .findOverlappingLeave(
                            anyLong(), any(), any()))
                    .thenReturn(new ArrayList<>());
            when(holidayRepository.existsByHolidayDate(
                    any()))
                    .thenReturn(false);
            when(leaveBalanceRepository
                    .findByUserIdAndLeaveTypeAndYear(
                            anyLong(), anyString(), anyInt()))
                    .thenReturn(Optional.of(mockLeaveBalance));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.applyLeave(
                        1L, leaveRequestDto));

            assertTrue(exception.getMessage()
                .contains("Insufficient leave balance"));
        }

        @Test
        @DisplayName("Should throw when no balance record found")
        void applyLeave_NoBalanceRecord() {
            when(leaveRequestRepository
                    .findOverlappingLeave(
                            anyLong(), any(), any()))
                    .thenReturn(new ArrayList<>());
            when(holidayRepository.existsByHolidayDate(
                    any()))
                    .thenReturn(false);
            when(leaveBalanceRepository
                    .findByUserIdAndLeaveTypeAndYear(
                            anyLong(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.applyLeave(
                        1L, leaveRequestDto));

            assertTrue(exception.getMessage()
                .contains("No leave balance found"));
        }

        @Test
        @DisplayName("Should throw when all days are holidays/weekends")
        void applyLeave_OnlyHolidaysSelected() {
            // Select a weekend
            leaveRequestDto.setFromDate(
                LocalDate.of(2026, 4, 4)); // Saturday
            leaveRequestDto.setToDate(
                LocalDate.of(2026, 4, 5)); // Sunday

            when(leaveRequestRepository
                    .findOverlappingLeave(
                            anyLong(), any(), any()))
                    .thenReturn(new ArrayList<>());
            when(holidayRepository.existsByHolidayDate(
                    any()))
                    .thenReturn(false);

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.applyLeave(
                        1L, leaveRequestDto));

            assertTrue(exception.getMessage()
                .contains("no working days"));
        }
    }

    // ═══════════════════════════════════════════════
    // REVIEW LEAVE TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Review Leave Tests")
    class ReviewLeaveTests {

        private LeaveReviewDto approveRequest;
        private LeaveReviewDto rejectRequest;

        @BeforeEach
        void setUp() {
            approveRequest = new LeaveReviewDto();
            approveRequest.setAction("APPROVED");
            approveRequest.setComment("Approved!");

            rejectRequest = new LeaveReviewDto();
            rejectRequest.setAction("REJECTED");
            rejectRequest.setComment("Not approved");
        }

        @Test
        @DisplayName("Should approve leave and deduct balance")
        void reviewLeave_Approve() {
            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));
            when(leaveRequestRepository.save(
                    any(LeaveRequest.class)))
                    .thenReturn(mockLeaveRequest);
            when(leaveBalanceRepository
                    .findByUserIdAndLeaveTypeAndYear(
                            anyLong(), anyString(), anyInt()))
                    .thenReturn(Optional.of(mockLeaveBalance));
            when(leaveBalanceRepository.save(
                    any(LeaveBalance.class)))
                    .thenReturn(mockLeaveBalance);
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            LeaveResponseDto response =
                leaveService.reviewLeave(
                    1L, 2L, approveRequest);

            assertNotNull(response);
            // Balance should be deducted
            verify(leaveBalanceRepository, times(1))
                    .save(any(LeaveBalance.class));
        }

        @Test
        @DisplayName("Should reject without deducting balance")
        void reviewLeave_Reject() {
            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));
            when(leaveRequestRepository.save(
                    any(LeaveRequest.class)))
                    .thenReturn(mockLeaveRequest);
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            leaveService.reviewLeave(1L, 2L, rejectRequest);

            // Balance should NOT be deducted on rejection
            verify(leaveBalanceRepository, never())
                    .save(any(LeaveBalance.class));
        }

        @Test
        @DisplayName("Should throw when rejecting without comment")
        void reviewLeave_RejectWithoutComment() {
            rejectRequest.setComment(null);

            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.reviewLeave(
                        1L, 2L, rejectRequest));

            assertEquals(
                "Comment is mandatory when rejecting",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should throw when leave is not submitted")
        void reviewLeave_NotSubmitted() {
            mockLeaveRequest.setStatus("APPROVED");

            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.reviewLeave(
                        1L, 2L, approveRequest));

            assertTrue(exception.getMessage()
                .contains("Only SUBMITTED"));
        }
    }

    // ═══════════════════════════════════════════════
    // CANCEL LEAVE TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Cancel Leave Tests")
    class CancelLeaveTests {

        @Test
        @DisplayName("Should cancel leave successfully")
        void cancelLeave_Success() {
            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));
            when(leaveRequestRepository.save(
                    any(LeaveRequest.class)))
                    .thenReturn(mockLeaveRequest);
            when(authServiceClient.getUserById(anyLong()))
                    .thenReturn(mockUserResponse);

            LeaveResponseDto response =
                leaveService.cancelLeave(1L, 1L);

            assertNotNull(response);
            verify(leaveRequestRepository, times(1))
                    .save(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("Should throw when user tries to cancel another's leave")
        void cancelLeave_NotOwner() {
            // Leave belongs to userId=1
            // But userId=2 trying to cancel
            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> {
                        when(leaveRequestRepository
                                .findById(1L))
                                .thenReturn(Optional.of(
                                    mockLeaveRequest));
                        leaveService.cancelLeave(2L, 1L);
                    });
        }

        @Test
        @DisplayName("Should throw when cancelling already cancelled leave")
        void cancelLeave_AlreadyCancelled() {
            mockLeaveRequest.setStatus("CANCELLED");

            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.cancelLeave(1L, 1L));

            assertTrue(exception.getMessage()
                .contains("Cannot cancel a CANCELLED"));
        }

        @Test
        @DisplayName("Should throw when cancelling past approved leave")
        void cancelLeave_PastApprovedLeave() {
            mockLeaveRequest.setStatus("APPROVED");
            mockLeaveRequest.setFromDate(
                LocalDate.now().minusDays(5));

            when(leaveRequestRepository.findById(1L))
                    .thenReturn(
                        Optional.of(mockLeaveRequest));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.cancelLeave(1L, 1L));

            assertTrue(exception.getMessage()
                .contains("already started"));
        }
    }

    // ═══════════════════════════════════════════════
    // HOLIDAY TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Holiday Tests")
    class HolidayTests {

        @Test
        @DisplayName("Should add holiday successfully")
        void addHoliday_Success() {
            HolidayDto dto = new HolidayDto();
            dto.setHolidayDate(LocalDate.of(2026, 8, 15));
            dto.setHolidayName("Independence Day");
            dto.setHolidayType("NATIONAL");

            Holiday holiday = Holiday.builder()
                    .id(1L)
                    .holidayDate(LocalDate.of(2026, 8, 15))
                    .holidayName("Independence Day")
                    .holidayType("NATIONAL")
                    .build();

            when(holidayRepository.existsByHolidayDate(
                    any()))
                    .thenReturn(false);
            when(holidayRepository.save(any(Holiday.class)))
                    .thenReturn(holiday);

            Holiday result = leaveService.addHoliday(dto);

            assertNotNull(result);
            assertEquals("Independence Day",
                result.getHolidayName());
        }

        @Test
        @DisplayName("Should throw when holiday date already exists")
        void addHoliday_DuplicateDate() {
            HolidayDto dto = new HolidayDto();
            dto.setHolidayDate(LocalDate.of(2026, 8, 15));
            dto.setHolidayName("Independence Day");

            when(holidayRepository.existsByHolidayDate(
                    any()))
                    .thenReturn(true);

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> leaveService.addHoliday(dto));

            assertTrue(exception.getMessage()
                .contains("Holiday already exists"));
        }
    }
}