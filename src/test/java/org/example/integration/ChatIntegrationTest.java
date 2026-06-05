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
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldShowOnlyCurrentUserChats() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);

        mockMvc.perform(get("/api/chats/my")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId", is(orderId.intValue())));
    }

    @Test
    void shouldSendMessageByCurrentUser() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);
        Long chatId = findChatIdByOrderId(buyerToken, orderId);

        mockMvc.perform(post("/api/chats/messages")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chatId": %d,
                                  "content": "Здравствуйте, хочу уточнить детали заказа",
                                  "imageUrl": ""
                                }
                                """.formatted(chatId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId", is(chatId.intValue())))
                .andExpect(jsonPath("$.content", is("Здравствуйте, хочу уточнить детали заказа")));
    }

    @Test
    void shouldAllowMessageAuthorToUpdateOwnMessage() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);
        Long chatId = findChatIdByOrderId(buyerToken, orderId);
        Long messageId = sendMessageAndReturnId(buyerToken, chatId, "Первый текст сообщения");

        mockMvc.perform(patch("/api/chats/messages/" + messageId + "/content")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Обновлённый текст сообщения"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/chats/" + chatId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].content", is("Обновлённый текст сообщения")));
    }

    @Test
    void shouldRejectUpdatingOtherUserMessage() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);
        Long chatId = findChatIdByOrderId(buyerToken, orderId);
        Long messageId = sendMessageAndReturnId(buyerToken, chatId, "Сообщение покупателя");

        mockMvc.perform(patch("/api/chats/messages/" + messageId + "/content")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "Продавец пытается изменить чужое сообщение"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only message sender can update this message")));
    }

    @Test
    void shouldRejectViewingOtherUserChat() throws Exception {
        String sellerToken = registerAndGetToken("seller_" + UUID.randomUUID(), "seller_" + UUID.randomUUID() + "@test.com");
        String buyerToken = registerAndGetToken("buyer_" + UUID.randomUUID(), "buyer_" + UUID.randomUUID() + "@test.com");
        String strangerToken = registerAndGetToken("stranger_" + UUID.randomUUID(), "stranger_" + UUID.randomUUID() + "@test.com");

        Long listingId = createListingAndReturnId(sellerToken);
        Long orderId = createOrderAndReturnId(buyerToken, listingId);
        Long chatId = findChatIdByOrderId(buyerToken, orderId);

        mockMvc.perform(get("/api/chats/" + chatId)
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Only chat participants can view this chat")));
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
                                  "title": "Test listing for chat",
                                  "description": "Test listing description",
                                  "price": 25.00,
                                  "currency": "USDT",
                                  "network": "POLYGON",
                                  "deliveryType": "MANUAL",
                                  "imageUrls": [
                                    "https://example.com/test-chat.png"
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

    private Long findChatIdByOrderId(String token, Long orderId) throws Exception {
        String response = mockMvc.perform(get("/api/chats/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode chats = objectMapper.readTree(response);

        for (JsonNode chat : chats) {
            if (chat.get("orderId").asLong() == orderId) {
                return chat.get("id").asLong();
            }
        }

        throw new IllegalStateException("Chat not found for order: " + orderId);
    }

    private Long sendMessageAndReturnId(String token, Long chatId, String content) throws Exception {
        String response = mockMvc.perform(post("/api/chats/messages")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chatId": %d,
                                  "content": "%s",
                                  "imageUrl": ""
                                }
                                """.formatted(chatId, content)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }
}