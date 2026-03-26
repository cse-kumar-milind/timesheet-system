package com.company.authservice.test;

import com.company.authservice.dto.*;
import com.company.authservice.model.User;
import com.company.authservice.repository.UserRepository;
import com.company.authservice.security.JwtService;
import com.company.authservice.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ✅ @ExtendWith(MockitoExtension.class) — tells JUnit
// to use Mockito for creating mocks automatically
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    // ✅ @Mock creates a fake/mock version of the class
    // No real database calls — all controlled by us
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    // ✅ @InjectMocks creates real AuthService
    // and injects all @Mock objects into it
    @InjectMocks
    private AuthService authService;

    // ✅ Test data — reused across tests
    private User mockUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Build a mock user for testing
        mockUser = User.builder()
                .id(1L)
                .employeeCode("EMP001")
                .fullName("John Doe")
                .email("john@example.com")
                .password("$2a$10$encodedPassword")
                .role("EMPLOYEE")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        // Build signup request
        signupRequest = new SignupRequest();
        signupRequest.setEmployeeCode("EMP001");
        signupRequest.setFullName("John Doe");
        signupRequest.setEmail("john@example.com");
        signupRequest.setPassword("Password@123");

        // Build login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("Password@123");
    }

    // ═══════════════════════════════════════════════
    // SIGNUP TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Signup Tests")
    class SignupTests {

        @Test
        @DisplayName("Should signup successfully")
        void signup_Success() {
            // ✅ ARRANGE — set up mock behavior
            // When userRepository.existsByEmail is called
            // with any string, return false (email not taken)
            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);
            when(userRepository.existsByEmployeeCode(
                    anyString()))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(mockUser);

            // ✅ ACT — call the method we're testing
            assertDoesNotThrow(() ->
                authService.signup(signupRequest));

            // ✅ ASSERT — verify interactions
            // verify() checks if a method was called
            verify(userRepository, times(1))
                    .save(any(User.class));
            verify(passwordEncoder, times(1))
                    .encode("Password@123");
        }

        @Test
        @DisplayName("Should throw exception when email exists")
        void signup_EmailAlreadyExists() {
            // Email already registered
            when(userRepository.existsByEmail(
                    "john@example.com"))
                    .thenReturn(true);

            // ✅ assertThrows — expects exception to be thrown
            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.signup(signupRequest));

            assertEquals(
                "Email already registered: john@example.com",
                exception.getMessage());

            // Verify save was NEVER called
            verify(userRepository, never())
                    .save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when employee code exists")
        void signup_EmployeeCodeAlreadyExists() {
            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);
            when(userRepository.existsByEmployeeCode(
                    "EMP001"))
                    .thenReturn(true);

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.signup(signupRequest));

            assertEquals(
                "Employee code already exists: EMP001",
                exception.getMessage());
        }

        @Test
        @DisplayName("Should always set role to EMPLOYEE")
        void signup_AlwaysSetsEmployeeRole() {
            when(userRepository.existsByEmail(anyString()))
                    .thenReturn(false);
            when(userRepository.existsByEmployeeCode(
                    anyString()))
                    .thenReturn(false);
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("encoded");

            // Capture the User object saved to DB
            // So we can verify its role
            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> {
                        User savedUser =
                            invocation.getArgument(0);
                        // ✅ Verify role is always EMPLOYEE
                        assertEquals("EMPLOYEE",
                            savedUser.getRole());
                        return savedUser;
                    });

            authService.signup(signupRequest);
        }
    }

    // ═══════════════════════════════════════════════
    // LOGIN TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully and return JWT")
        void login_Success() {
            // Authentication succeeds (no exception thrown)
            when(authenticationManager.authenticate(
                    any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null); // return value ignored

            when(userRepository.findByEmail(
                    "john@example.com"))
                    .thenReturn(Optional.of(mockUser));

            when(jwtService.generateToken(mockUser))
                    .thenReturn("mocked.jwt.token");

            AuthResponse response =
                authService.login(loginRequest);

            // ✅ Assert response values
            assertNotNull(response);
            assertEquals("mocked.jwt.token",
                response.getToken());
            assertEquals("john@example.com",
                response.getEmail());
            assertEquals("John Doe",
                response.getFullName());
            assertEquals("EMPLOYEE",
                response.getRole());
            assertEquals(1L, response.getUserId());
        }

        @Test
        @DisplayName("Should throw exception for invalid credentials")
        void login_InvalidCredentials() {
            // Authentication fails — throws exception
            when(authenticationManager.authenticate(
                    any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException(
                            "Bad credentials"));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.login(loginRequest));

            assertEquals("Invalid email or password",
                exception.getMessage());

            // JWT should never be generated
            verify(jwtService, never())
                    .generateToken(any());
        }

        @Test
        @DisplayName("Should throw exception when user not found after auth")
        void login_UserNotFound() {
            when(authenticationManager.authenticate(
                    any()))
                    .thenReturn(null);

            when(userRepository.findByEmail(anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
        }
    }

    // ═══════════════════════════════════════════════
    // CHANGE ROLE TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Change Role Tests")
    class ChangeRoleTests {

        private User adminUser;
        private User employeeUser;
        private ChangeRoleRequest changeRoleRequest;

        @BeforeEach
        void setUp() {
            adminUser = User.builder()
                    .id(1L)
                    .email("admin@company.com")
                    .role("ADMIN")
                    .status("ACTIVE")
                    .build();

            employeeUser = User.builder()
                    .id(2L)
                    .email("john@example.com")
                    .role("EMPLOYEE")
                    .status("ACTIVE")
                    .fullName("John Doe")
                    .employeeCode("EMP001")
                    .build();

            changeRoleRequest = new ChangeRoleRequest();
            changeRoleRequest.setRole("MANAGER");
        }

        @Test
        @DisplayName("Admin should successfully change user role")
        void changeRole_Success() {
            when(userRepository.findByEmail(
                    "admin@company.com"))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(2L))
                    .thenReturn(Optional.of(employeeUser));
            when(userRepository.save(any(User.class)))
                    .thenReturn(employeeUser);

            UserResponse response = authService.changeRole(
                    2L,
                    changeRoleRequest,
                    "admin@company.com");

            assertNotNull(response);
            verify(userRepository, times(1))
                    .save(any(User.class));
        }

        @Test
        @DisplayName("Non-admin should not change role")
        void changeRole_NotAdmin() {
            User nonAdmin = User.builder()
                    .id(3L)
                    .email("employee@company.com")
                    .role("EMPLOYEE")
                    .build();

            when(userRepository.findByEmail(
                    "employee@company.com"))
                    .thenReturn(Optional.of(nonAdmin));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.changeRole(
                        2L,
                        changeRoleRequest,
                        "employee@company.com"));

            assertEquals("Only ADMIN can change roles",
                exception.getMessage());
        }

        @Test
        @DisplayName("Admin should not change own role")
        void changeRole_AdminCannotChangeOwnRole() {
            when(userRepository.findByEmail(
                    "admin@company.com"))
                    .thenReturn(Optional.of(adminUser));
            when(userRepository.findById(1L))
                    .thenReturn(Optional.of(adminUser));

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.changeRole(
                        1L,
                        changeRoleRequest,
                        "admin@company.com"));

            assertEquals(
                "Admin cannot change their own role",
                exception.getMessage());
        }
    }

    // ═══════════════════════════════════════════════
    // FORGOT PASSWORD TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Forgot Password Tests")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Should reset password successfully")
        void forgotPassword_Success() {
            ForgotPasswordRequest request =
                new ForgotPasswordRequest();
            request.setEmail("john@example.com");
            request.setNewPassword("NewPass@123");
            request.setConfirmPassword("NewPass@123");

            when(userRepository.findByEmail(
                    "john@example.com"))
                    .thenReturn(Optional.of(mockUser));
            when(passwordEncoder.encode(anyString()))
                    .thenReturn("$2a$10$newEncodedPassword");

            assertDoesNotThrow(() ->
                authService.forgotPassword(request));

            verify(userRepository, times(1))
                    .save(any(User.class));
        }

        @Test
        @DisplayName("Should throw when passwords don't match")
        void forgotPassword_PasswordMismatch() {
            ForgotPasswordRequest request =
                new ForgotPasswordRequest();
            request.setEmail("john@example.com");
            request.setNewPassword("NewPass@123");
            request.setConfirmPassword("Different@123");

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.forgotPassword(request));

            assertEquals("Passwords do not match",
                exception.getMessage());

            verify(userRepository, never())
                    .save(any());
        }

        @Test
        @DisplayName("Should throw when email not found")
        void forgotPassword_EmailNotFound() {
            ForgotPasswordRequest request =
                new ForgotPasswordRequest();
            request.setEmail("unknown@example.com");
            request.setNewPassword("NewPass@123");
            request.setConfirmPassword("NewPass@123");

            when(userRepository.findByEmail(
                    "unknown@example.com"))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                () -> authService.forgotPassword(request));
        }
    }

    // ═══════════════════════════════════════════════
    // GET ALL USERS TESTS
    // ═══════════════════════════════════════════════

    @Nested
    @DisplayName("Get Users Tests")
    class GetUsersTests {

        @Test
        @DisplayName("Should return all users")
        void getAllUsers_Success() {
            User user2 = User.builder()
                    .id(2L)
                    .employeeCode("EMP002")
                    .fullName("Jane Doe")
                    .email("jane@example.com")
                    .role("EMPLOYEE")
                    .status("ACTIVE")
                    .build();

            when(userRepository.findAll())
                    .thenReturn(List.of(mockUser, user2));

            List<UserResponse> users =
                authService.getAllUsers();

            assertNotNull(users);
            assertEquals(2, users.size());
            assertEquals("john@example.com",
                users.get(0).getEmail());
            assertEquals("jane@example.com",
                users.get(1).getEmail());
        }

        @Test
        @DisplayName("Should return empty list when no users")
        void getAllUsers_EmptyList() {
            when(userRepository.findAll())
                    .thenReturn(List.of());

            List<UserResponse> users =
                authService.getAllUsers();

            assertNotNull(users);
            assertTrue(users.isEmpty());
        }

        @Test
        @DisplayName("Should return user by id")
        void getUserById_Success() {
            when(userRepository.findById(1L))
                    .thenReturn(Optional.of(mockUser));

            UserResponse response =
                authService.getUserById(1L);

            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("john@example.com",
                response.getEmail());
        }

        @Test
        @DisplayName("Should throw when user id not found")
        void getUserById_NotFound() {
            when(userRepository.findById(99L))
                    .thenReturn(Optional.empty());

            RuntimeException exception =
                assertThrows(RuntimeException.class,
                    () -> authService.getUserById(99L));

            assertEquals("User not found with id: 99",
                exception.getMessage());
        }
    }
}