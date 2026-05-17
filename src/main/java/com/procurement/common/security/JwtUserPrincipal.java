package com.procurement.common.security;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

public record JwtUserPrincipal(
  String email,
  Set<String> roles,
  Set<String> permissions
) implements Principal {
  public static final String REQUEST_ATTRIBUTE = JwtUserPrincipal.class.getName();

  public JwtUserPrincipal {
    roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
    permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
  }

  @Override
  public String getName() {
    return email;
  }

  public boolean hasAnyPermission(String... requiredPermissions) {
    if (requiredPermissions == null || requiredPermissions.length == 0) {
      return true;
    }

    for (String requiredPermission : requiredPermissions) {
      if (requiredPermission != null && permissions.contains(requiredPermission)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasAnyRole(String... requiredRoles) {
    if (requiredRoles == null || requiredRoles.length == 0) {
      return true;
    }

    for (String requiredRole : requiredRoles) {
      if (requiredRole != null && roles.contains(requiredRole)) {
        return true;
      }
    }
    return false;
  }
}
