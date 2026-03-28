
package com.company.authservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.authservice.dto.AuthResponse;
import com.company.authservice.dto.ChangeRoleRequest;
import com.company.authservice.dto.ChangeStatusRequest;
import com.company.authservice.dto.ForgotPasswordRequest;
import com.company.authservice.dto.LoginRequest;
import com.company.authservice.dto.SignupRequest;
import com.company.authservice.dto.UpdateManagerRequest;
import com.company.authservice.dto.UserResponse;
import com.company.authservice.event.EventPublisher;
import com.company.authservice.event.UserRegisteredEvent;
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
	private final EventPublisher eventPublisher;
	
	public String signup(SignupRequest request) {
		
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
                .role("EMPLOYEE")
                .managerId(request.getManagerId())
                .status("ACTIVE")
                .build();
		
		userRepository.save(user);
		
		eventPublisher.publishUserRegistered(UserRegisteredEvent.builder()
		        .userId(user.getId())
		        .email(user.getEmail())
		        .fullName(user.getFullName())
		        .employeeCode(user.getEmployeeCode())
		        .role(user.getRole())
		        .build());
	
		
		return "Registration successful!\nHello "+user.getFullName();
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
				 	.userId(user.getId())
	                .email(user.getEmail())
	                .fullName(user.getFullName())
	                .role(user.getRole())
	                .build();
	}
	
	// ─── Forgot Password ───────────────────────────────

    public void forgotPassword(
            ForgotPasswordRequest request) {
    	
    	User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "No account found with email: "
                        + request.getEmail()));
    	
        // Check passwords match
        if (!request.getNewPassword()
                    .equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                "Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(
                request.getNewPassword()));
        userRepository.save(user);
    }
    
 // ─── Get Profile ───────────────────────────────────

    public UserResponse getProfile(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found"));
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("User not found with email: " + email);
        }
        userRepository.deleteByEmail(email);
    }
    
    public UserResponse updateManager(
    		Long userId,
            String email,
            UpdateManagerRequest request) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: "+userId));
        
        if (user.getEmail()
                .equals(email)) {
        	throw new RuntimeException(
          "Admin cannot set their own manager");
        }
        
        user.setManagerId(request.getManagerId());
        

        userRepository.save(user);
        return mapToUserResponse(user);
    }
    
 // ─── Change Role/Status (Admin only) ──────────────────────

    public UserResponse changeRole(
            Long userId,
            ChangeRoleRequest request,
            String requestedByEmail) {

        // ✅ Find target user
        User targetUser = userRepository
                .findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId));

        // Prevent admin from changing their own role
        if (targetUser.getEmail()
                      .equals(requestedByEmail)) {
            throw new RuntimeException(
                "Admin cannot change their own role");
        }

        String oldRole = targetUser.getRole();
        targetUser.setRole(request.getRole());
        userRepository.save(targetUser);

		eventPublisher.publishUserRoleChanged(UserRegisteredEvent.builder()
		        .userId(targetUser.getId())
		        .email(targetUser.getEmail())
		        .fullName(targetUser.getFullName())
		        .employeeCode(targetUser.getEmployeeCode())
		        .role(targetUser.getRole())
		        .build());

        System.out.println("Role changed: "
            + targetUser.getEmail()
            + " from " + oldRole
            + " to " + request.getRole()
            + " by " + requestedByEmail);

        return mapToUserResponse(targetUser);
    }
    
    public UserResponse changeStatus(Long userId, ChangeStatusRequest request, String requestedByEmail) {
    	
    	User user = userRepository.findById(userId)
    				.orElseThrow(() -> new RuntimeException("User not found with id: "+userId));
    	
    	// Prevent admin from changing their own status
        if (user.getEmail()
                      .equals(requestedByEmail)) {
            throw new RuntimeException(
                "Admin cannot change their own status");
        }
        
    	user.setStatus(request.getStatus());
    	
    	userRepository.save(user);
    	
    	return mapToUserResponse(user);
    }
	
	// ─── Internal Methods ──────────────────────────────

	public UserResponse getUserById(Long id) {
	    User user = userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException(
	                    "User not found with id: " + id));
	    return mapToUserResponse(user);
	}

	public List<UserResponse> getAllUsers() {
	    return userRepository.findAll()
	            .stream()
	            .map(this::mapToUserResponse)
	            .collect(Collectors.toList());
	}

	private UserResponse mapToUserResponse(User user) {
	    return UserResponse.builder()
	            .id(user.getId())
	            .employeeCode(user.getEmployeeCode())
	            .fullName(user.getFullName())
	            .email(user.getEmail())
	            .role(user.getRole())
	            .status(user.getStatus())
	            .managerId(user.getManagerId())
	            .createdAt(user.getCreatedAt())
	            .build();
	}
}
