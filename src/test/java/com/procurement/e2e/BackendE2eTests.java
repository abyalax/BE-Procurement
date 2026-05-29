package com.procurement.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackendE2eTests extends AbstractE2eTest {

  @Test
  @Order(0)
  void rejectsProtectedEndpointsWithoutAuthentication() {
    Assertions.assertThrows(HttpClientErrorException.Unauthorized.class, () ->
      getJsonEntity("/api/v1/users", null)
    );
  }

  @Test
  @Order(1)
  void loginAndFetchCurrentUserThroughHttp() throws Exception {
    JsonNode login = postJson(
      "/api/v1/auth/login",
      Map.of("email", "admin@procurement.local", "password", "password")
    );

    assertThat(login.path("success").asBoolean()).isTrue();
    assertThat(login.path("data").path("tokenType").asText()).isEqualTo("Bearer");

    String token = login.path("data").path("accessToken").asText();
    assertThat(token).isNotBlank();

    ResponseEntity<String> response = getJson("/api/v1/users/me", token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode me = objectMapper.readTree(response.getBody());
    assertThat(me.path("success").asBoolean()).isTrue();
    assertThat(me.path("data").path("email").asText()).isEqualTo("admin@procurement.local");
    assertThat(arrayText(me.path("data").path("roles"))).contains("ADMIN");
    assertThat(arrayText(me.path("data").path("permissions"))).contains("users:read");
  }

  @Test
  @Order(2)
  void writesAreVisibleInsideCurrentTest() throws Exception {
    String token = adminToken();

    ResponseEntity<String> createResponse = postJsonEntity(
      "/api/v1/vendors",
      Map.of(
        "name",
        "Temporary E2E Vendor",
        "email",
        "temporary-e2e-vendor@example.test",
        "performanceScore",
        "91.25"
      ),
      token
    );

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<String> listResponse = getJson("/api/v1/vendors", token);
    JsonNode vendors = objectMapper.readTree(listResponse.getBody());

    assertThat(vendors.path("data").path("meta").path("totalItems").asLong()).isEqualTo(4);
  }

  @Test
  @Order(3)
  void databaseAndRedisAreResetBetweenTests() throws Exception {
    String token = adminToken();

    ResponseEntity<String> response = getJson("/api/v1/vendors", token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode vendors = objectMapper.readTree(response.getBody());
    assertThat(vendors.path("data").path("meta").path("totalItems").asLong()).isEqualTo(3);
    assertThat(response.getBody()).doesNotContain("temporary-e2e-vendor@example.test");
  }

  @Test
  @Order(4)
  void paginatedListEndpointsHonorSortParamsThroughHttp() throws Exception {
    String token = adminToken();

    JsonNode vendors = assertSuccessfulList(
      "/api/v1/vendors?sort_by=performance_score&sort_order=DESC",
      token
    );
    assertThat(vendors.path("data").path(0).path("name").asText()).isEqualTo(
      "Prima Logistics Services"
    );
    assertThat(vendors.path("meta").path("sortBy").path(0).path(0).asText()).isEqualTo(
      "performanceScore"
    );
    assertThat(vendors.path("meta").path("sortBy").path(0).path(1).asText()).isEqualTo("DESC");

    assertSuccessfulList("/api/v1/users?sort_by=email&sort_order=ASC", token);
    assertSuccessfulList("/api/v1/pr?sort_by=title&sort_order=ASC", token);
    assertSuccessfulList("/api/v1/rfqs?sort_by=deadline&sort_order=DESC", token);
    assertSuccessfulList("/api/v1/qcfs?sort_by=recommended_vendor_name&sort_order=ASC", token);
    assertSuccessfulList("/api/v1/purchase-orders?sort_by=vendor_name&sort_order=ASC", token);
    assertSuccessfulList("/api/v1/invoices?sort_by=invoice_number&sort_order=ASC", token);
  }

  @Test
  @Order(5)
  void vendorsSupportUpdateSearchStatusFilterAndSoftDeactivate() throws Exception {
    String token = adminToken();

    JsonNode created = postData(
      "/api/v1/vendors",
      Map.of(
        "name",
        "Status Filter E2E Vendor",
        "email",
        "status-filter-e2e-vendor@example.test",
        "status",
        "INACTIVE",
        "performanceScore",
        "70.25"
      ),
      token
    );
    long vendorId = created.path("id").asLong();
    assertThat(created.path("status").asText()).isEqualTo("INACTIVE");

    ResponseEntity<String> updateResponse = patchJsonEntity(
      "/api/v1/vendors/" + vendorId,
      Map.of(
        "name",
        "Updated Status Filter E2E Vendor",
        "email",
        "updated-status-filter-e2e-vendor@example.test",
        "status",
        "ACTIVE",
        "performanceScore",
        "93.50"
      ),
      token
    );
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode updatedBody = objectMapper.readTree(updateResponse.getBody());
    JsonNode updated = updatedBody.path("data");
    assertThat(updated.path("name").asText()).isEqualTo("Updated Status Filter E2E Vendor");
    assertThat(updated.path("email").asText()).isEqualTo(
      "updated-status-filter-e2e-vendor@example.test"
    );
    assertThat(updated.path("status").asText()).isEqualTo("ACTIVE");
    assertThat(updated.path("performanceScore").decimalValue()).isEqualByComparingTo("93.50");

    JsonNode activeVendors = assertSuccessfulList(
      "/api/v1/vendors?status=ACTIVE&search=updated-status-filter",
      token
    );
    assertThat(activeVendors.path("meta").path("totalItems").asLong()).isEqualTo(1);
    assertThat(activeVendors.path("data").path(0).path("id").asLong()).isEqualTo(vendorId);

    ResponseEntity<String> deleteResponse = deleteJsonEntity("/api/v1/vendors/" + vendorId, token);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode inactiveVendors = assertSuccessfulList(
      "/api/v1/vendors?status=INACTIVE&search=updated-status-filter",
      token
    );
    assertThat(inactiveVendors.path("meta").path("totalItems").asLong()).isEqualTo(1);
    assertThat(inactiveVendors.path("data").path(0).path("id").asLong()).isEqualTo(vendorId);
    assertThat(inactiveVendors.path("data").path(0).path("status").asText()).isEqualTo("INACTIVE");
  }

  @Test
  @Order(6)
  void completesProcurementHappyPathThroughHttp() throws Exception {
    String token = adminToken();

    JsonNode pr = postData(
      "/api/v1/pr",
      Map.of(
        "title",
        "Office Laptop Procurement",
        "description",
        "Laptop purchase for finance team",
        "department",
        "Finance",
        "requestedBy",
        user("employee@procurement.local"),
        "emergency",
        false,
        "justification",
        "Replacement devices",
        "items",
        List.of(
          prItem("GOODS", "Finance laptop", "9", "150000"),
          prItem("SERVICE", "Device setup service", "1", "150000")
        )
      ),
      token
    );
    long prId = pr.path("id").asLong();
    assertThat(pr.path("status").asText()).isEqualTo("DRAFT");
    assertThat(pr.path("totalEstimatedAmount").decimalValue()).isEqualByComparingTo("1500000");
    assertThat(pr.path("items")).hasSize(2);

    JsonNode submittedPr = postData(
      "/api/v1/pr/" + prId + "/submit",
      decision("employee@procurement.local", "Ready for approval"),
      token
    );
    assertThat(submittedPr.path("status").asText()).isEqualTo("SUBMITTED");

    JsonNode approvedPr = postData(
      "/api/v1/pr/" + prId + "/approve",
      decision("supervisor@procurement.local", "Approved"),
      token
    );
    assertThat(approvedPr.path("status").asText()).isEqualTo("APPROVED");
    assertThat(approvedPr.path("stage").asText()).isEqualTo("RFQ Preparation");

    JsonNode rfq = postData(
      "/api/v1/rfqs",
      Map.of(
        "prId",
        prId,
        "title",
        "Laptop RFQ",
        "description",
        "Request vendor quotations for laptops",
        "deadline",
        LocalDateTime.now().plusDays(7).toString(),
        "vendorIds",
        List.of(1, 2),
        "actor",
        user("purchasing.staff@procurement.local")
      ),
      token
    );
    long rfqId = rfq.path("id").asLong();
    assertThat(rfq.path("status").asText()).isEqualTo("ACTIVE");
    assertThat(rfq.path("items")).hasSize(2);
    assertThat(rfq.path("items").path(0).path("itemName").asText()).isEqualTo("Finance laptop");

    JsonNode invitation = postData(
      "/api/v1/rfqs/" + rfqId + "/vendors/1/accept",
      decision("purchasing.staff@procurement.local", "Vendor accepted"),
      token
    );
    assertThat(invitation.path("status").asText()).isEqualTo("ACCEPTED");

    JsonNode quotation = postData(
      "/api/v1/rfqs/" + rfqId + "/quotations",
      Map.of(
        "vendorId",
        1,
        "amount",
        "1500000",
        "leadTimeDays",
        14,
        "technicalScore",
        "90",
        "commercialScore",
        "88",
        "notes",
        "Best commercial offer"
      ),
      token
    );
    assertThat(quotation.path("version").asInt()).isEqualTo(1);

    JsonNode qcf = postData(
      "/api/v1/qcfs",
      Map.of(
        "rfqId",
        rfqId,
        "actor",
        user("purchasing.staff@procurement.local"),
        "notes",
        "Prepared comparison"
      ),
      token
    );
    long qcfId = qcf.path("id").asLong();
    assertThat(qcf.path("status").asText()).isEqualTo("DRAFT");
    assertThat(qcf.path("recommendedVendorId").asLong()).isEqualTo(1);

    JsonNode approvedQcf = postData(
      "/api/v1/qcfs/" + qcfId + "/approve",
      decision("purchasing.manager@procurement.local", "Award approved"),
      token
    );
    assertThat(approvedQcf.path("status").asText()).isEqualTo("APPROVED");

    JsonNode po = postData(
      "/api/v1/qcfs/" + qcfId + "/award",
      decision("purchasing.manager@procurement.local", "Award vendor"),
      token
    );
    long poId = po.path("id").asLong();
    assertThat(po.path("status").asText()).isEqualTo("OPEN");
    assertThat(po.path("remainingQuantity").decimalValue()).isEqualByComparingTo("10");

    JsonNode receipt = postData(
      "/api/v1/purchase-orders/" + poId + "/goods-receipts",
      Map.of(
        "actor",
        user("warehouse.staff@procurement.local"),
        "receivedQuantity",
        "10",
        "notes",
        "Full delivery received"
      ),
      token
    );
    assertThat(receipt.path("receivedQuantity").decimalValue()).isEqualByComparingTo("10");

    JsonNode invoice = postData(
      "/api/v1/invoices/po/" + poId,
      Map.of(
        "invoiceNumber",
        "INV-E2E-001",
        "amount",
        "1500000",
        "actor",
        user("finance.staff@procurement.local"),
        "notes",
        "Invoice received"
      ),
      token
    );
    long invoiceId = invoice.path("id").asLong();
    assertThat(invoice.path("status").asText()).isEqualTo("SUBMITTED");

    JsonNode verifiedInvoice = postData(
      "/api/v1/invoices/" + invoiceId + "/verify",
      decision("finance.staff@procurement.local", "Matched"),
      token
    );
    assertThat(verifiedInvoice.path("status").asText()).isEqualTo("VERIFIED");
    assertThat(verifiedInvoice.path("matchStatus").asText()).isEqualTo("MATCHED");

    JsonNode paidInvoice = postData(
      "/api/v1/invoices/" + invoiceId + "/pay",
      decision("finance.manager@procurement.local", "Payment released"),
      token
    );
    assertThat(paidInvoice.path("status").asText()).isEqualTo("PAID");

    JsonNode audit = objectMapper.readTree(
      getJson("/api/v1/audit/INVOICE/" + invoiceId, token).getBody()
    );
    assertThat(audit.path("data")).anyMatch(activity ->
      "PAYMENT_RECORDED".equals(activity.path("activityType").asText())
    );
  }

  @Test
  @Order(7)
  void exposesSeededProcurementRolesThroughHttp() throws Exception {
    String token = adminToken();

    JsonNode roles = objectMapper.readTree(getJson("/api/v1/access/roles", token).getBody());

    assertThat(roles.path("success").asBoolean()).isTrue();
    assertThat(arrayText(roles.path("data").findValues("name"))).contains(
      "EMPLOYEE",
      "SUPERVISOR",
      "PURCHASING_STAFF",
      "WAREHOUSE_STAFF",
      "FINANCE_MANAGER"
    );
  }

  @Test
  @Order(8)
  void rejectsCancellingApprovedPurchaseRequisition() throws Exception {
    String token = adminToken();

    JsonNode pr = postData(
      "/api/v1/pr",
      Map.of(
        "title",
        "Cancellation Guard PR",
        "department",
        "Operations",
        "requestedBy",
        user("employee@procurement.local"),
        "emergency",
        false,
        "justification",
        "Transition guard",
        "items",
        List.of(prItem("SERVICE", "Transition support", "1", "1500000"))
      ),
      token
    );
    long prId = pr.path("id").asLong();
    postData(
      "/api/v1/pr/" + prId + "/submit",
      decision("employee@procurement.local", "Ready"),
      token
    );
    postData(
      "/api/v1/pr/" + prId + "/approve",
      decision("supervisor@procurement.local", "Approved"),
      token
    );

    Assertions.assertThrows(HttpClientErrorException.BadRequest.class, () ->
      postJsonEntity(
        "/api/v1/pr/" + prId + "/cancel",
        decision("employee@procurement.local", "Too late"),
        token
      )
    );
  }

  @Test
  @Order(9)
  void usersSupportCreateUpdateAndSoftDeactivate() throws Exception {
    String token = adminToken();
    String suffix = String.valueOf(System.currentTimeMillis());
    String email = "users-e2e-" + suffix + "@example.test";
    String updatedEmail = "users-e2e-updated-" + suffix + "@example.test";

    JsonNode created = postData(
      "/api/v1/users",
      Map.of("name", "Users E2E " + suffix, "email", email, "password", "password123"),
      token
    );
    long userId = created.path("id").asLong();
    assertThat(created.path("status").asText()).isEqualTo("ACTIVE");

    ResponseEntity<String> updateResponse = patchJsonEntity(
      "/api/v1/users/" + userId,
      Map.of("name", "Users E2E Updated " + suffix, "email", updatedEmail, "status", "ACTIVE"),
      token
    );
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode updatedBody = objectMapper.readTree(updateResponse.getBody());
    JsonNode updated = updatedBody.path("data");
    assertThat(updated.path("name").asText()).isEqualTo("Users E2E Updated " + suffix);
    assertThat(updated.path("email").asText()).isEqualTo(updatedEmail);
    assertThat(updated.path("status").asText()).isEqualTo("ACTIVE");

    ResponseEntity<String> deleteResponse = deleteJsonEntity("/api/v1/users/" + userId, token);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode fetched = objectMapper.readTree(getJson("/api/v1/users/" + userId, token).getBody());
    assertThat(fetched.path("data").path("status").asText()).isEqualTo("INACTIVE");
    assertThat(fetched.path("data").path("email").asText()).isEqualTo(updatedEmail);
  }

  private static List<String> arrayText(JsonNode node) {
    List<String> values = new ArrayList<>();
    node.forEach(value -> values.add(value.asText()));
    return values;
  }

  private static List<String> arrayText(List<JsonNode> nodes) {
    List<String> values = new ArrayList<>();
    nodes.forEach(value -> values.add(value.asText()));
    return values;
  }

  private JsonNode postData(String path, Object request, String token) throws Exception {
    ResponseEntity<String> response = postJsonEntity(path, request, token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("success").asBoolean()).isTrue();
    return body.path("data");
  }

  private JsonNode assertSuccessfulList(String path, String token) throws Exception {
    ResponseEntity<String> response = getJson(path, token);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.path("success").asBoolean()).isTrue();
    assertThat(body.path("data").path("data").isArray()).isTrue();
    return body.path("data");
  }

  private static Map<String, Object> decision(String actor, String notes) {
    return Map.of("actor", user(actor), "notes", notes);
  }

  private static Map<String, Object> prItem(
    String itemType,
    String itemName,
    String quantity,
    String estimatedUnitPrice
  ) {
    return Map.of(
      "itemType",
      itemType,
      "itemName",
      itemName,
      "specification",
      itemName + " specification",
      "quantity",
      quantity,
      "unitOfMeasure",
      "EA",
      "estimatedUnitPrice",
      estimatedUnitPrice
    );
  }

  private static Map<String, Object> user(String email) {
    return Map.of(
      "id",
      1L,
      "name",
      email.substring(0, email.indexOf('@')).replace('.', ' '),
      "email",
      email,
      "roles",
      List.of("ADMIN"),
      "permissions",
      List.of()
    );
  }
}
