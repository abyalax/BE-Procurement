package com.procurement.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {
    "spring.liquibase.enabled=true",
    "app.jwt.secret=test-jwt-secret-with-at-least-thirty-two-characters",
    "app.jwt.expiration-ms=86400000",
  }
)
abstract class AbstractE2eTest {

  private static final String ADMIN_EMAIL = "admin@procurement.local";
  private static final String ADMIN_PASSWORD = "password";

  @LocalServerPort
  private int port;

  protected final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private RedisConnectionFactory redisConnectionFactory;

  @BeforeEach
  void resetExternalState() {
    SecurityContextHolder.clearContext();
    flushRedis();
    truncateApplicationTables();
    seedDefaultData();
  }

  protected JsonNode postJson(String path, Object request) throws Exception {
    ResponseEntity<String> response = postJsonEntity(path, request, null);
    return objectMapper.readTree(response.getBody());
  }

  protected ResponseEntity<String> getJson(String path, String token) {
    HttpHeaders headers = jsonHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return restClient()
      .get()
      .uri(path)
      .headers(target -> target.addAll(headers))
      .retrieve()
      .toEntity(String.class);
  }

  protected ResponseEntity<String> getJsonEntity(String path, String token) {
    return getJson(path, token);
  }

  protected ResponseEntity<String> postJsonEntity(String path, Object request, String token) {
    HttpHeaders headers = jsonHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return restClient()
      .post()
      .uri(path)
      .headers(target -> target.addAll(headers))
      .body(request)
      .retrieve()
      .toEntity(String.class);
  }

  protected ResponseEntity<String> patchJsonEntity(String path, Object request, String token) {
    HttpHeaders headers = jsonHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return restClient()
      .patch()
      .uri(path)
      .headers(target -> target.addAll(headers))
      .body(request)
      .retrieve()
      .toEntity(String.class);
  }

  protected ResponseEntity<String> deleteJsonEntity(String path, String token) {
    HttpHeaders headers = jsonHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return restClient()
      .delete()
      .uri(path)
      .headers(target -> target.addAll(headers))
      .retrieve()
      .toEntity(String.class);
  }

  protected String adminToken() throws Exception {
    JsonNode login = postJson(
      "/api/v1/auth/login",
      Map.of("email", ADMIN_EMAIL, "password", ADMIN_PASSWORD)
    );
    return login.path("data").path("accessToken").asText();
  }

  private HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    return headers;
  }

  private RestClient restClient() {
    return RestClient.builder().baseUrl("http://localhost:" + port).build();
  }

  private void flushRedis() {
    try (RedisConnection connection = redisConnectionFactory.getConnection()) {
      connection.serverCommands().flushDb();
    }
  }

  private void truncateApplicationTables() {
    jdbcTemplate.execute(
      """
      TRUNCATE TABLE
        audit_activities,
        approval_steps,
        approval_workflows,
        invoices,
        goods_receipts,
        purchase_orders,
        qcf_lines,
        qcf_documents,
        vendor_quotations,
        rfq_vendor_invitations,
        rfq_items,
        rfqs,
        purchase_requisition_items,
        purchase_requisitions,
        vendors,
        user_roles,
        role_permissions,
        permissions,
        roles,
        users
      RESTART IDENTITY CASCADE
      """
    );
  }

  private void seedDefaultData() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
      new ClassPathResource("db/changelog/changes/003-seed-default-access-data.sql"),
      new ClassPathResource("db/changelog/changes/004-seed-procurement-roles.sql"),
      new ClassPathResource("db/changelog/changes/005-seed-role-users.sql")
    );
    populator.execute(dataSource);
  }
}
