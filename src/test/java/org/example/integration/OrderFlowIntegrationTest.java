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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    private static final String PAYMENT_TX_HASH_1 =
            "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";

    private static final String PAYMENT_TX_HASH_2 =
            "0xabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd";

    private static final String RELEASE_TX_HASH =
            "0xfedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrderFromJwtAndMoveListingToSold() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": %d
                                }
                                """.formatted(listingId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listingId", is(listingId.intValue())))
                .andExpect(jsonPath("$.status", is("CREATED")));

        mockMvc.perform(get("/api/listings/" + listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SOLD")));
    }

    @Test
    void shouldRejectBuyingOwnListing() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": %d
                                }
                                """.formatted(listingId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Seller cannot buy own listing")));
    }

    @Test
    void shouldRejectBuyingSoldListing() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String firstBuyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");
        String secondBuyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + firstBuyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": %d
                                }
                                """.formatted(listingId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + secondBuyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": %d
                                }
                                """.formatted(listingId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Listing is not active")));
    }

    @Test
    void shouldMoveOrderThroughPaidSellerDoneCompleted() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);

        mockMvc.perform(patch("/api/orders/" + orderId + "/paid")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentTxHash": "%s"
                                }
                                """.formatted(PAYMENT_TX_HASH_1)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAID")))
                .andExpect(jsonPath("$.paymentTxHash", is(PAYMENT_TX_HASH_1)));

        mockMvc.perform(patch("/api/orders/" + orderId + "/seller-done"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SELLER_DONE")));

        mockMvc.perform(patch("/api/orders/" + orderId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "releaseTxHash": "%s"
                                }
                                """.formatted(RELEASE_TX_HASH)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.releaseTxHash", is(RELEASE_TX_HASH)));
    }

    @Test
    void shouldAllowCancelOnlyCreatedOrder() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void shouldRejectCancelPaidOrder() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);

        mockMvc.perform(patch("/api/orders/" + orderId + "/paid")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentTxHash": "%s"
                                }
                                """.formatted(PAYMENT_TX_HASH_2)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only CREATED order can be cancelled")));
    }

    @Test
    void shouldRejectInvalidPaymentTxHash() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);

        mockMvc.perform(patch("/api/orders/" + orderId + "/paid")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentTxHash": "0xpaid"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid payment transaction hash format")));
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
                                  "title": "Test listing",
                                  "description": "Test listing description",
                                  "price": 25.00,
                                  "currency": "USDT",
                                  "network": "POLYGON",
                                  "deliveryType": "MANUAL",
                                  "imageUrls": [
                                    "https://example.com/test.png"
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

    private Long createOrderAndReturnId(String buyerToken, Long listingId) throws Exception {
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

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}