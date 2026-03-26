package com.company.authservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.authservice.dto.AuthResponse;
import com.company.authservice.dto.ChangeRoleRequest;
import com.company.authservice.dto.ChangeStatusRequest;
import com.company.authservice.dto.ForgotPasswordRequest;
import com.company.authservice.dto.LoginRequest;
import com.company.authservice.dto.SignupRequest;
import com.company.authservice.dto.UpdateManagerRequest;
import com.company.authservice.dto.UserResponse;
import com.company.authservice.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	
	@PostMapping("/signup")
	public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request){
		
		
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
		
		AuthResponse authResponse = authService.login(request);
		
		return ResponseEntity.ok(authResponse);
	}
	
	@PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody
                ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
            "Password updated successfully!");
    }
	
	
	
	@GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile( @AuthenticationPrincipal UserDetails userDetails) {
        
		return ResponseEntity.ok(
            authService.getProfile(
                userDetails.getUsername()));
    }
	
	@PatchMapping("/users/{id}/manager")
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateManager(@PathVariable Long id,
            @AuthenticationPrincipal
                UserDetails userDetails,
            @Valid @RequestBody
                UpdateManagerRequest request) {
        return ResponseEntity.ok(
            authService.updateManager(
                id, userDetails.getUsername(), request));
    }
	
	@PatchMapping("/users/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> changeRole(
	        @PathVariable Long id, @Valid @RequestBody ChangeRoleRequest request,
	        @AuthenticationPrincipal UserDetails userDetails) {

	    return ResponseEntity.ok(
	        authService.changeRole(
	            id,
	            request,
	            userDetails.getUsername()));
	}

	@PatchMapping("/users/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> changeStatus(
			@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request,
			@AuthenticationPrincipal UserDetails userDetails){
		
		return ResponseEntity.ok(authService.changeStatus(id, request, userDetails.getUsername()));
	}
	
	
	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
            authService.getAllUsers());
    }

	@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {
    	
        
        return ResponseEntity.ok(
            authService.getUserById(id));
    }
	
	@GetMapping("/health-check")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> healthCheck(){
		return ResponseEntity.ok("Auth Service is running!");
	}
}
