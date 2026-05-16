package com.procurement.modules.role_permissions.controller;

import com.procurement.common.response.ApiResponse;
import com.procurement.common.security.Guard;
import com.procurement.modules.role_permissions.dto.CreateRoleRequest;
import com.procurement.modules.role_permissions.dto.PermissionResponse;
import com.procurement.modules.role_permissions.dto.RoleResponse;
import com.procurement.modules.role_permissions.dto.UpdateRolePermissionsRequest;
import com.procurement.modules.role_permissions.services.RolePermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/access")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/roles")
    @Guard({"roles:read"})
    public ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.success("Roles fetched successfully", rolePermissionService.getRoles());
    }

    @PostMapping("/roles")
    @Guard({"roles:write"})
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success("Role created successfully",
                rolePermissionService.createRole(request));
    }

    @PutMapping("/roles/{id}/permissions")
    @Guard({"roles:write"})
    public ApiResponse<RoleResponse> updateRolePermissions(@PathVariable Long id,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        return ApiResponse.success("Role permissions updated successfully",
                rolePermissionService.updateRolePermissions(id, request));
    }

    @GetMapping("/permissions")
    @Guard({"permissions:read"})
    public ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.success("Permissions fetched successfully",
                rolePermissionService.getPermissions());
    }
}
