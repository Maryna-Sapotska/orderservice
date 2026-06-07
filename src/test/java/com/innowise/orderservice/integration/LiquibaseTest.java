package com.innowise.orderservice.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class LiquibaseTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        assertTrue(postgres.isRunning());
    }
}
