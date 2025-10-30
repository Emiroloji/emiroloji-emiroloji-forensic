package com.forensic.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Login Response DTO
 */
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;
    private String sessionId;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, String tokenType,
            Long expiresIn, UserInfo user, String sessionId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public String getToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * User Info nested class
     */
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String status;
        private List<String> roles;
        private boolean twoFactorEnabled;
        private LocalDateTime lastLogin;

        // Constructors
        public UserInfo() {
        }

        public UserInfo(String id, String username, String email, String firstName,
                String lastName, String status, List<String> roles,
                boolean twoFactorEnabled, LocalDateTime lastLogin) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.status = status;
            this.roles = roles;
            this.twoFactorEnabled = twoFactorEnabled;
            this.lastLogin = lastLogin;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public boolean isTwoFactorEnabled() {
            return twoFactorEnabled;
        }

        public void setTwoFactorEnabled(boolean twoFactorEnabled) {
            this.twoFactorEnabled = twoFactorEnabled;
        }

        public LocalDateTime getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    public String getId() {
        return user != null ? user.getId() : null;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
}
