package com.jats.dto;

import com.jats.entity.User;
import java.util.Set;

public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private Set<User.Role> roles;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String fullName, Set<User.Role> roles) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<User.Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<User.Role> roles) {
        this.roles = roles;
    }
}
