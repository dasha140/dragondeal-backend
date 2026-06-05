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
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateReviewByCurrentUser() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long sellerId = order.get("sellerId").asLong();

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "targetUserId": %d,
                                  "rating": 5,
                                  "comment": "Продавец быстро ответил"
                                }
                                """.formatted(orderId, sellerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId.intValue())))
                .andExpect(jsonPath("$.targetUserId", is(sellerId.intValue())))
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Продавец быстро ответил")));
    }

    @Test
    void shouldRejectReviewingYourself() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long buyerId = order.get("buyerId").asLong();

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "targetUserId": %d,
                                  "rating": 5,
                                  "comment": "Сам себе отзыв"
                                }
                                """.formatted(orderId, buyerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User cannot review themselves")));
    }

    @Test
    void shouldAllowReviewAuthorToUpdateOwnReview() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long sellerId = order.get("sellerId").asLong();
        Long reviewId = createReviewAndReturnId(buyerToken, orderId, sellerId);

        mockMvc.perform(patch("/api/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 4,
                                  "comment": "Обновил свой отзыв через тест"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reviews/" + reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(4)))
                .andExpect(jsonPath("$.comment", is("Обновил свой отзыв через тест")));
    }

    @Test
    void shouldRejectUpdatingOtherUserReview() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");
        String strangerToken = registerAndGetToken("stranger_" + UUID.randomUUID(), "stranger_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        JsonNode order = createOrderAndReturnJson(buyerToken, listingId);

        Long orderId = order.get("id").asLong();
        Long sellerId = order.get("sellerId").asLong();
        Long reviewId = createReviewAndReturnId(buyerToken, orderId, sellerId);

        mockMvc.perform(patch("/api/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + strangerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 1,
                                  "comment": "Пытаюсь изменить чужой отзыв"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only review author can update this review")));
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
                                  "title": "Test listing for review",
                                  "description": "Test listing description",
                                  "price": 25.00,
                                  "currency": "USDT",
                                  "network": "POLYGON",
                                  "deliveryType": "MANUAL",
                                  "imageUrls": [
                                    "https://example.com/test-review.png"
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

    private Long createReviewAndReturnId(String buyerToken, Long orderId, Long targetUserId) throws Exception {
        String response = mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": %d,
                                  "targetUserId": %d,
                                  "rating": 5,
                                  "comment": "Первый текст отзыва"
                                }
                                """.formatted(orderId, targetUserId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}