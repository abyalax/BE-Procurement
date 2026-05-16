package com.procurement.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class GuardAspect {

    @Around("@annotation(com.procurement.common.security.Guard) || @within(com.procurement.common.security.Guard)")
    public Object guard(ProceedingJoinPoint joinPoint) throws Throwable {
        Guard guard = resolveGuard(joinPoint);
        JwtUserPrincipal principal = resolvePrincipal();

        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }

        if (guard != null && guard.roles().length > 0 && !principal.hasAnyRole(guard.roles())) {
            throw new AccessDeniedException(
                    "Missing required roles: " + Arrays.toString(guard.roles()));
        }

        if (guard != null && guard.permissions().length > 0
                && !principal.hasAnyPermission(guard.permissions())) {
            throw new AccessDeniedException(
                    "Missing required permissions: " + Arrays.toString(guard.permissions()));
        }

        return joinPoint.proceed();
    }

    private JwtUserPrincipal resolvePrincipal() {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            Object requestPrincipal = request.getAttribute(JwtUserPrincipal.REQUEST_ATTRIBUTE);
            if (requestPrincipal instanceof JwtUserPrincipal principal) {
                return principal;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserPrincipal principal) {
            return principal;
        }

        return null;
    }

    private Guard resolveGuard(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Guard guard = AnnotationUtils.findAnnotation(method, Guard.class);
        if (guard != null) {
            return guard;
        }

        Class<?> targetClass = joinPoint.getTarget().getClass();
        return AnnotationUtils.findAnnotation(targetClass, Guard.class);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
