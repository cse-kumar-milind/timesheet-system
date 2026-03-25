package com.company.adminservice.client;

import com.company.adminservice.dto.TimesheetResponse;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TimesheetServiceClientFallback
        implements TimesheetServiceClient {

    @Override
    public List<TimesheetResponse> getPendingTimesheets(
            String role) {
        return List.of();
    }
}