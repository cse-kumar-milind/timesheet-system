package com.company.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    private String role;
    private String status;
    private Long managerId;
    private LocalDateTime createdAt;
}