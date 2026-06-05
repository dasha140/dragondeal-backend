package org.example.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DisputeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateDisputeByCurrentUser() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();

        mockMvc.perform(post("/api/disputes")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "reason": "Продавец не передал товар после оплаты"
                                }
                                """.formatted(orderId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId.intValue())))
                .andExpect(jsonPath("$.reason", is("Продавец не передал товар после оплаты")))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    void shouldRejectCreatingDisputeForOtherUserOrder() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");
        String strangerToken = registerAndGetToken("stranger_" + UUID.randomUUID(), "stranger_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();

        mockMvc.perform(post("/api/disputes")
                        .header("Authorization", "Bearer " + strangerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "reason": "Пытаюсь открыть спор по чужому заказу"
                                }
                                """.formatted(orderId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User is not a participant of this order")));
    }

    @Test
    void shouldAddEvidenceByCurrentUser() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long disputeId = createDisputeAndReturnId(buyerToken, orderId);

        mockMvc.perform(post("/api/disputes/" + disputeId + "/evidence")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Я написал продавцу в чат, но ответа нет",
                                  "imageUrl": "https://example.com/evidence/chat.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disputeId", is(disputeId.intValue())))
                .andExpect(jsonPath("$.text", is("Я написал продавцу в чат, но ответа нет")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/evidence/chat.png")));
    }

    @Test
    void shouldRejectViewingOtherUserDispute() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");
        String strangerToken = registerAndGetToken("stranger_" + UUID.randomUUID(), "stranger_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long disputeId = createDisputeAndReturnId(buyerToken, orderId);

        mockMvc.perform(get("/api/disputes/" + disputeId)
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only dispute order participants can view this dispute")));
    }

    @Test
    void shouldRejectResolveByRegularUser() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long disputeId = createDisputeAndReturnId(buyerToken, orderId);

        mockMvc.perform(patch("/api/disputes/" + disputeId + "/resolve")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminDecision": "Пытаюсь решить спор обычным пользователем"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only admin can resolve disputes")));
    }

    @Test
    void shouldRejectRejectByRegularUser() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long disputeId = createDisputeAndReturnId(buyerToken, orderId);

        mockMvc.perform(patch("/api/disputes/" + disputeId + "/reject")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminDecision": "Пытаюсь отклонить спор обычным пользователем"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only admin can reject disputes")));
    }

    private String registerAndGetToken(String username, String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "12345678"
                                }
                                """.formatted(username, email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("accessToken").asText();
    }

    private Long createListingAndReturnId(String sellerToken) throws Exception {
        String response = mockMvc.perform(post("/api/listings")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "gameId": 3,
                                  "title": "Test listing for dispute",
                                  "description": "Test listing description",
                                  "price": 25.00,
                                  "currency": "USDT",
                                  "network": "POLYGON",
                                  "deliveryType": "MANUAL",
                                  "imageUrls": [
                                    "https://example.com/test-dispute.png"
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private JsonNode createOrderAndReturnJson(String buyerToken, Long listingId) throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": %d
                                }
                                """.formatted(listingId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private Long createDisputeAndReturnId(String token, Long orderId) throws Exception {
        String response = mockMvc.perform(post("/api/disputes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "reason": "Тестовая причина спора"
                                }
                                """.formatted(orderId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}