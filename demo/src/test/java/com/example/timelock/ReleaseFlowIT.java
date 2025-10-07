package com.example.timelock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReleaseFlowIT {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("appdb")
      .withUsername("appuser")
      .withPassword("apppass");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", mysql::getJdbcUrl);
    r.add("spring.datasource.username", mysql::getUsername);
    r.add("spring.datasource.password", mysql::getPassword);

    // Make JwtService happy in tests
    r.add("jwt.issuer", () -> "example.com");
    // Any 32+ byte Base64 string works for HS256
    r.add("jwt.secret", () -> "8evdgMoilLs4kfweAyXSh3LDTi0fdk6ru+d9NRpFto0=");
  }

  @Autowired
  TestRestTemplate http;

  @Test
  void loginAndCreateRelease() {
    // login
    ResponseEntity<Map> login = http.postForEntity(
        "/auth/login",
        Map.of("email", "user@example.com", "password", "user123"),
        Map.class);

    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    String token = (String) login.getBody().get("token");
    assertThat(token).isNotBlank();

    // create release
    HttpHeaders h = new HttpHeaders();
    h.setBearerAuth(token);
    h.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> create = http.exchange(
        "/api/v1/releases",
        HttpMethod.POST,
        new HttpEntity<>(Map.of("title","IT","description","d","payloadJson","{}"), h),
        Map.class);

    assertThat(create.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(create.getBody()).containsEntry("title", "IT");
  }
}
