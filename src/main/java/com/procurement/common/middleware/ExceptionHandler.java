package com.procurement.common.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurement.common.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        write(response, HttpStatus.UNAUTHORIZED, "Authentication required", request);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        write(response, HttpStatus.FORBIDDEN, "Access denied", request);
    }

    private void write(HttpServletResponse response, HttpStatus status, String message,
            HttpServletRequest request) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(),
                ErrorResponse.of(message, request.getRequestURI(), Map.of()));
    }
}
