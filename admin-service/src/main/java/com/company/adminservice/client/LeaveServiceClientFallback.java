package com.company.adminservice.client;

import com.company.adminservice.dto.LeaveResponseDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class LeaveServiceClientFallback
        implements LeaveServiceClient {

    @Override
    public List<LeaveResponseDto> getPendingLeaves(
            String role) {
        return List.of();
    }
}