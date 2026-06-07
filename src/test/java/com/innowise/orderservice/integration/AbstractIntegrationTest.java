package com.innowise.orderservice.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    static final WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.5.2");

    static {
        postgres.start();
        wiremock.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {

        registry.add("services.users-service.url", wiremock::getBaseUrl);
    }

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupWireMock() {

        WireMock.configureFor(
                wiremock.getHost(),
                wiremock.getPort()
        );
        WireMock.reset();
    }

    @AfterEach
    void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE order_items RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE orders RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE items RESTART IDENTITY CASCADE");
    }

    protected void stubUser(Long userId) {
        WireMock.stubFor(WireMock.get("/users/" + userId)
                .willReturn(WireMock.okJson("""
        {
          "id": %d,
          "name": "John",
          "surname": "Doe",
          "email": "john@test.com",
          "active": true
        }
    """.formatted(userId))));
    }

    protected void stubUserError() {
        WireMock.stubFor(
                WireMock.any(WireMock.urlMatching("/users/.*"))
                        .willReturn(WireMock.serverError())
        );
    }
}
