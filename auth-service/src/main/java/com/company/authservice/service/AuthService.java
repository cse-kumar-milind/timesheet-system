package com.company.authservice.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.company.authservice.dto.AuthResponse;
import com.company.authservice.dto.LoginRequest;
import com.company.authservice.dto.SignupRequest;
import com.company.authservice.model.User;
import com.company.authservice.repository.UserRepository;
import com.company.authservice.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	
	public AuthResponse signup(SignupRequest request) {
		
		if(userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException(
					"Email already registered: "+request.getEmail());
		}
		
		if(userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
			throw new RuntimeException(
					"Employee code already exists: "+request.getEmployeeCode());
		}
		
		User user = User.builder()
				.employeeCode(request.getEmployeeCode())
				.fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .managerId(request.getManagerId())
                .status("ACTIVE")
                .build();
		
		User savedUser = userRepository.save(user);
		
		String token = jwtService.generateToken(savedUser);
		
		return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .userId(savedUser.getId())
                .build();
	}
	
	public AuthResponse login(LoginRequest request) {
		
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()));
		}
		catch (AuthenticationException e) {
			throw new RuntimeException(
					"Invalid email or password");
		}
		
		 User user = userRepository.findByEmail(request.getEmail())
				 .orElseThrow(() -> 
				 new RuntimeException("User not found"));
		 
		 String token = jwtService.generateToken(user);
		 
		 return AuthResponse.builder()
				 .token(token)
	                .email(user.getEmail())
	                .fullName(user.getFullName())
	                .role(user.getRole())
	                .userId(user.getId())
	                .build();
	}
}
