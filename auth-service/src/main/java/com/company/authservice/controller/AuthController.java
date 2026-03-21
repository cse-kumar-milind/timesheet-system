package com.company.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.authservice.dto.AuthResponse;
import com.company.authservice.dto.LoginRequest;
import com.company.authservice.dto.SignupRequest;
import com.company.authservice.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	
	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request){
		
		AuthResponse authResponse = authService.signup(request);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
		
		AuthResponse authResponse = authService.login(request);
		
		return ResponseEntity.ok(authResponse);
	}
	
	@GetMapping("/health-check")
	public ResponseEntity<String> healthCheck(){
		return ResponseEntity.ok("Auth Service is running!");
	}
}
