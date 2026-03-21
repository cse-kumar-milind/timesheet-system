package com.company.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2-100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // "EMPLOYEE" / "MANAGER" / "ADMIN"
    @NotBlank(message = "Role is required")
    @Pattern(
        regexp = "EMPLOYEE|MANAGER|ADMIN",
        message = "Role must be EMPLOYEE, MANAGER, or ADMIN"
    )
    private String role;

    // Optional — only for EMPLOYEE role
    private Long managerId;
}