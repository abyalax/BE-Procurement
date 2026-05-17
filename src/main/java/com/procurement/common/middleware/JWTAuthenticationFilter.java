package com.procurement.common.middleware;

import com.procurement.common.security.JwtUserPrincipal;
import com.procurement.modules.auth.services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

  private final JWTService jwtService;

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    try {
      if (
        jwtService.isTokenValid(token) &&
        SecurityContextHolder.getContext().getAuthentication() == null
      ) {
        JwtUserPrincipal principal = jwtService.extractPrincipal(token);
        request.setAttribute(JwtUserPrincipal.REQUEST_ATTRIBUTE, principal);

        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(principal, null, buildAuthorities(principal));

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception exception) {
      SecurityContextHolder.clearContext();
      request.removeAttribute(JwtUserPrincipal.REQUEST_ATTRIBUTE);
    }

    filterChain.doFilter(request, response);
  }

  private Set<org.springframework.security.core.GrantedAuthority> buildAuthorities(
    JwtUserPrincipal principal
  ) {
    LinkedHashSet<org.springframework.security.core.GrantedAuthority> authorities =
      new LinkedHashSet<>();
    for (String role : principal.roles()) {
      authorities.add(
        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
      );
    }
    for (String permission : principal.permissions()) {
      authorities.add(
        new org.springframework.security.core.authority.SimpleGrantedAuthority(permission)
      );
    }
    return authorities;
  }
}
