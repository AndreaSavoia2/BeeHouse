package com.prj.beehouse.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Sends an error response to the client with HTTP status 401 (Unauthorized)
        // and a standard text message in the response body
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");
    }
}

