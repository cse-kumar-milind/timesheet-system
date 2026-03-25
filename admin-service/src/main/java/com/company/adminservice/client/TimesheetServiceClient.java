package com.company.adminservice.client;

import com.company.adminservice.dto.TimesheetResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

@FeignClient(
    name = "timesheet-service",
    fallback = TimesheetServiceClientFallback.class
)
public interface TimesheetServiceClient {

    // ✅ Pass X-User-Role header so Timesheet Service
    //    allows manager/admin access
    @GetMapping("/timesheet/manager/pending")
    List<TimesheetResponse> getPendingTimesheets(
            @RequestHeader("X-User-Role") String role);
}