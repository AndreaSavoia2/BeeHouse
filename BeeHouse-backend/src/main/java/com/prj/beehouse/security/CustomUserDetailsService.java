package com.prj.beehouse.security;

import com.prj.beehouse.entity.User;
import com.prj.beehouse.exception.BadRequestException;
import com.prj.beehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserDetailsService} used by Spring Security
 * to load user-specific data during the authentication process.
 * <p>
 * Users are retrieved from the database using their email address and
 * converted into {@link CustomUserDetails} instances.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository used to retrieve user information from the database.
     */
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        if(!user.isEnable())
            throw new BadRequestException("User is not enabled.");
        // Return our CustomUserDetails so controllers can inject it with @AuthenticationPrincipal
        return CustomUserDetails.build(user);
    }
}
