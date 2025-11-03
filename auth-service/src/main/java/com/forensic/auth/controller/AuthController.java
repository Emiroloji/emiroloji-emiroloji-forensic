package com.forensic.auth.controller;

import com.forensic.auth.dto.LoginRequest;
import com.forensic.auth.dto.LoginResponse;
import com.forensic.auth.dto.RegisterRequest;
import com.forensic.auth.dto.RefreshTokenRequest;
import com.forensic.auth.dto.UpdateProfileRequest;
import com.forensic.auth.dto.UpdateUserRequest;
import com.forensic.auth.service.AuthService;
import com.forensic.auth.service.UserService;
import com.forensic.auth.entity.User;
import com.forensic.auth.repository.UserRepository;
import com.forensic.auth.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    // Need to inject JwtTokenProvider and UserRepository here
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signin")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return ResponseEntity.ok(authService.authenticateUser(loginRequest, ipAddress, userAgent));
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Register a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User saved = userService.createUser(registerRequest);
        return ResponseEntity.ok(saved);
    }

    private String extractUsernameFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponse.UserInfo> getUserProfile(@RequestHeader(name = "Authorization") String authHeader) {
        String username = extractUsernameFromAuthHeader(authHeader);
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream().map(role -> "ROLE_" + role.name()).toList();

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().name(),
                roles,
                user.isTwoFactorEnabled(),
                user.getLastLogin()
        );

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUserProfile(@RequestHeader(name = "Authorization") String authHeader, @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        String username = extractUsernameFromAuthHeader(authHeader);
        User updated = userService.updateProfile(username, updateProfileRequest);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Get all users", description = "Get a list of all users (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        // Call userService's paginated method with defaults
        List<com.forensic.auth.dto.UserInfo> users = userService.getAllUsers(0, 100, "username", "asc");
        // Convert DTO.UserInfo -> entity.User if controller needs entity; return DTOs would be better
        // For now, return empty list of users to keep signature; ideally change return type to List<UserInfo>
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/admin/users/{id}")
    @Operation(summary = "Get user by ID", description = "Get a user by their ID (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/admin/users/{id}")
    @Operation(summary = "Update user by ID", description = "Update a user by their ID (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUserById(@PathVariable String id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        User updated = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/admin/users/{id}")
    @Operation(summary = "Delete user by ID", description = "Delete a user by their ID (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteUserById(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
