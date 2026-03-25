package com.company.adminservice.client;

import com.company.adminservice.dto.LeaveResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

@FeignClient(
    name = "leave-service",
    fallback = LeaveServiceClientFallback.class
)
public interface LeaveServiceClient {

    @GetMapping("/leave/manager/pending")
    List<LeaveResponseDto> getPendingLeaves(
            @RequestHeader("X-User-Role") String role);
}