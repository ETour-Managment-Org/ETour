package com.example.demo.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.entities.User;

import lombok.Getter;

@Getter
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Integer userId;
    private final String username;
    private final String password;
    private final String email;
    private final String roleName;
    private final boolean active;

    public UserPrincipal(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.email = user.getEmail();
        this.roleName = user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER";
        this.active = user.getIsactive() == null || Boolean.TRUE.equals(user.getIsactive());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
