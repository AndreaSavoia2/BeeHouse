package com.prj.beehouse.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration class.
 * <p>
 * Configures JWT-based authentication, stateless session management,
 * password encoding, and endpoint authorization rules.
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    /**
     * Handles unauthorized access attempts and authentication failures.
     */
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    /**
     * Creates and registers the JWT authentication filter.
     * <p>
     * This filter intercepts incoming requests and validates JWT tokens
     * before the request reaches the authentication layer.
     *
     * @return the JWT authentication filter
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Exposes the {@link AuthenticationManager} bean used by Spring Security
     * to authenticate user credentials.
     *
     * @param authenticationConfiguration the authentication configuration
     * @return the authentication manager
     * @throws Exception if the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Creates the password encoder bean used to hash user passwords.
     * <p>
     * BCrypt is used to securely store passwords.
     *
     * @return a BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the Spring Security filter chain.
     * <p>
     * The configuration:
     * <ul>
     *     <li>Disables CSRF protection.</li>
     *     <li>Uses a custom authentication entry point for unauthorized requests.</li>
     *     <li>Enforces stateless session management.</li>
     *     <li>Allows unrestricted access to authentication endpoints and Swagger resources.</li>
     *     <li>Enforces role-based access on the role-first path spaces
     *         ({@code /api/client/**}, {@code /api/veterinary/**}, {@code /api/receptionist/**},
     *         {@code /api/staff/**}) as defense-in-depth on top of method-level security.</li>
     *     <li>Requires authentication for all other endpoints.</li>
     *     <li>Adds the JWT authentication filter before the username/password filter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} object used to configure security
     * @return the configured security filter chain
     * @throws Exception if an error occurs during security configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(s ->
                        s.sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(a ->
                        a.requestMatchers("/api/auth/**",
                                        "/error",
                                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                        "/api/public/**", "/ws/**").permitAll()
                                .requestMatchers("/api/user/**").hasAuthority("USER")
                                .requestMatchers("/api/administrator/**").hasAuthority("ADMINISTRATOR")
                                .requestMatchers("/api/shared/**").authenticated()
                                .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
