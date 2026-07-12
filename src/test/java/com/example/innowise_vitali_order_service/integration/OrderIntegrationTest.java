package com.example.innowise_vitali_order_service.integration;

import com.example.innowise_vitali_order_service.entity.Item;
import com.example.innowise_vitali_order_service.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"CREATE_PAYMENT", "PAYMENT_RESULT"})
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class OrderIntegrationTest {

    static final int WIRE_MOCK_PORT = 8082;
    private static WireMockServer wireMockServer;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    private Item savedItem;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WIRE_MOCK_PORT);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
        registry.add("spring.cloud.openfeign.client.config.user-service.url",
                () -> "http://localhost:" + WIRE_MOCK_PORT);
    }

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        savedItem = itemRepository.save(
                Item.builder()
                        .name("Test Item")
                        .price(new java.math.BigDecimal("10.00"))
                        .build()
        );
    }

    @Test
    void createOrder_shouldReturn400_whenNoUserId() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"itemId\":" + savedItem.getId() + ",\"quantity\":1}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/9999"))
                .andExpect(status().isNotFound());
    }
}
