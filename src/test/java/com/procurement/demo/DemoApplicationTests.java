package com.procurement.demo;

import com.procurement.modules.role_permissions.repositories.PermissionRepository;
import com.procurement.modules.role_permissions.repositories.RoleRepository;
import com.procurement.modules.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void seedDataExists() {
		assertThat(roleRepository.existsByName("ADMIN")).isTrue();
		assertThat(roleRepository.existsByName("USER")).isTrue();
		assertThat(permissionRepository.existsByKey("users:read")).isTrue();
		assertThat(userRepository.existsByEmail("admin@procurement.local")).isTrue();
	}

}
