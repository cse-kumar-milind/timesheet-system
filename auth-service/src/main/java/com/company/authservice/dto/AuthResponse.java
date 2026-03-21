package com.company.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private String fullName;
    private String role;
    private Long userId;

    // What the client receives after login:
    // {
    //   "token":    "eyJhbGc...",
    //   "email":    "john@example.com",
    //   "fullName": "John Doe",
    //   "role":     "EMPLOYEE",
    //   "userId":   1
    // }
}