package com.prj.beehouse.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * Custom security filter that intercepts every incoming HTTP request.
 * <p>
 * The filter extracts the JWT token from the {@code Authorization} header,
 * validates it, and, if successful, authenticates the corresponding user
 * within the Spring Security context.
 * <p>
 * Extends {@link OncePerRequestFilter} to ensure that the filter is executed
 * only once per request.
 */
@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    /**
     * Standard prefix used for JWT tokens in the HTTP Authorization header.
     */
    public static final String BEARER = "Bearer ";

    /**
     * Utility component responsible for JWT generation and validation.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Service used to load user details from the persistence layer.
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Intercepts the HTTP request, validates the JWT token, and sets
     * the authentication information in the Spring Security context.
     * <p>
     * If a valid token is found:
     * <ul>
     *     <li>The user identifier is extracted from the token.</li>
     *     <li>User details are loaded from the database.</li>
     *     <li>An authentication object is created.</li>
     *     <li>The authenticated user is stored in the {@link SecurityContextHolder}.</li>
     * </ul>
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param filterChain the filter chain to continue processing the request
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs while processing the request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extracts the raw JWT from the "Authorization" header
            String jwt = parseJwt(request);
            // Validates the token and checks it isn't null
            if (jwt != null && jwtUtil.validateJwtToken(jwt)) {
                // Retrieves the user's identity (username) encoded in the token
                final String username = jwtUtil.getUserFromToken(jwt);
                // Loads the user's details from the database
                final CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);
                // Builds Spring Security's main authentication token
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // Credentials (password) are no longer needed after JWT validation
                                userDetails.getAuthorities()
                        );
                // Attaches request details (IP, session id, etc.) to the authentication
                authenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Stores the authenticated user in the SecurityContextHolder, making it
                // available to the rest of the application
                SecurityContextHolder
                        .getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e);
        }
        // Continues the chain, passing the request to the next filter
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the {@code Authorization} header.
     * <p>
     * If the header starts with the {@value #BEARER} prefix,
     * the prefix is removed and the token is returned.
     *
     * @param request the HTTP request containing the Authorization header
     * @return the JWT token if present and properly formatted;
     *         {@code null} otherwise
     */
    private String parseJwt(HttpServletRequest request) {
        // Extracts the content of the Authorization header from the HTTP request
        String headerAuth = request.getHeader("Authorization");
        // Checks the header is present and has the standard prefix
        if (headerAuth != null && headerAuth.startsWith(BEARER)) {
            return headerAuth.substring(BEARER.length());
        }
        return null;
    }
}
