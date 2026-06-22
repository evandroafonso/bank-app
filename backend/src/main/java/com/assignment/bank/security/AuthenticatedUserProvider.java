package com.assignment.bank.security;

import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.service.UserService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class AuthenticatedUserProvider {

    private final UserService userService;

    public AuthenticatedUserProvider(UserService userService) {
        this.userService = userService;
    }

    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated or security context missing.");
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername());
        }

        throw new IllegalStateException("The principal is not an instance of UserDetails.");
    }
}
