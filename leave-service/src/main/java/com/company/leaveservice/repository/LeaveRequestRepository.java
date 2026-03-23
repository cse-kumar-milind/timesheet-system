package com.company.leaveservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.company.leaveservice.entity.LeaveRequest;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
	
	List<LeaveRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
	
	//All pending requests for manager approval
	List<LeaveRequest> findByStatus(String status);
	
	//list of pending requests for a specific manager
	List<LeaveRequest> findByManagerIdAndStatus(Long managerId, String status);
	
	// Check if employee already has approved/submitted
    // leave that overlaps with requested dates
	@Query("SELECT l FROM LeaveRequest l "
			+ "WHERE l.userId = :userId "
			+ "AND l.status IN ('SUBMITTED', 'APPROVED') "
			+ "AND l.fromDate <= :toDate "
			+ "AND l.toDate >= :fromDate ")
	List<LeaveRequest> findOverlappingLeave(@Param("userId") Long userId,
											@Param("fromDate") LocalDate fromDate,
											@Param("toDate") LocalDate toDate);
	
	// Leave history by type
    List<LeaveRequest> findByUserIdAndLeaveType(Long userId, String leaveType);
}
