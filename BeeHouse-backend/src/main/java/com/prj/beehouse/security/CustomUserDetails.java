package com.prj.beehouse.security;

import com.prj.beehouse.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom implementation of {@link UserDetails} used by Spring Security.
 * <p>
 * This class wraps the application's {@link User} entity and adapts it to the
 * contract expected by Spring Security during authentication and authorization.
 * <p>
 * It exposes user credentials and authorities and allows authenticated users
 * to be injected into controllers through {@code @AuthenticationPrincipal}.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    /**
     * The application user associated with this security principal.
     */
    private User user;

    /**
     * Creates a {@code CustomUserDetails} instance from a {@link User} entity.
     *
     * @param user the user entity to wrap
     * @return a new {@code CustomUserDetails} instance
     */
    public static CustomUserDetails build(User user) {
        return new CustomUserDetails(user);
    }

    /**
     * Returns the authorities granted to the user.
     * <p>
     * If no authority is associated with the user, an empty collection is returned.
     *
     * @return a collection containing the user's granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null || user.getAuthority() == null || user.getAuthority().getAuthorityName() == null)
            return List.of();
        return List.of(new SimpleGrantedAuthority(user.getAuthority().getAuthorityName().name()));
    }

    /**
     * Returns the user's encoded password.
     *
     * @return the encoded password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used for authentication.
     * <p>
     * In this application, the user's email address is used as the username.
     *
     * @return the user's email address
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
