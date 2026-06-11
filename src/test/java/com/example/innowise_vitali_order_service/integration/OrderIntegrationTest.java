package com.example.innowise_vitali_order_service.integration;

import com.example.innowise_vitali_order_service.dto.request.CreateOrderRequest;
import com.example.innowise_vitali_order_service.dto.request.OrderItemRequest;
import com.example.innowise_vitali_order_service.entity.Item;
import com.example.innowise_vitali_order_service.repository.ItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb").withUsername("test").withPassword("test");

    static WireMockServer wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ItemRepository itemRepository;

    private Item savedItem;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.cloud.openfeign.client.config.user-service.url", () -> "http://localhost:" + wireMock.port());
    }

    @BeforeAll static void startWireMock() { wireMock.start(); }
    @AfterAll  static void stopWireMock()  { wireMock.stop(); }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        itemRepository.deleteAll();

        savedItem = itemRepository.save(Item.builder()
                .name("Test Item").price(new BigDecimal("25.00")).build());

        wireMock.stubFor(get(urlPathEqualTo("/api/users/by-email"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"email\":\"1@placeholder.com\",\"firstName\":\"Test\",\"lastName\":\"User\"}")
                        .withStatus(200)));
    }

    @Test
    void createOrder_shouldReturn201() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(savedItem.getId(), 3)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(75.0));
    }

    @Test
    void createOrder_shouldReturn400_whenNoUserId() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"itemId\":1,\"quantity\":1}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.userId").exists());
    }

    @Test
    void getOrder_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_shouldSoftDelete_andReturn404OnSecondGet() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, List.of(new OrderItemRequest(savedItem.getId(), 1)));

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isNotFound());
    }
}