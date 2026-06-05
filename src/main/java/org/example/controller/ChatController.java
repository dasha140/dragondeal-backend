package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.Chat;
import org.example.domain.ChatMessage;
import org.example.dto.request.CreateChatMessageByCurrentUserRequest;
import org.example.dto.request.UpdateMessageContentRequest;
import org.example.dto.request.UpdateMessageImageRequest;
import org.example.dto.response.ChatMessageResponse;
import org.example.dto.response.ChatResponse;
import org.example.mapper.ChatMapper;
import org.example.service.ChatService;
import org.example.service.CurrentUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatMapper chatMapper;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<ChatResponse> findAllChats(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Chat> chats = chatService.findAllChatsForAdmin(currentUser);
        return chatMapper.toResponseList(chats);
    }

    @GetMapping("/my")
    public List<ChatResponse> findMyChats(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Chat> chats = chatService.findMyChats(currentUser);
        return chatMapper.toResponseList(chats);
    }

    @GetMapping("/{chatId}")
    public ChatResponse findChatWithMessages(
            HttpServletRequest httpRequest,
            @PathVariable Long chatId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        Chat chat = chatService.findChatWithMessagesForCurrentUser(currentUser, chatId);
        return chatMapper.toResponse(chat);
    }

    @GetMapping("/order/{orderId}")
    public List<ChatResponse> findChatsByOrder(@PathVariable Long orderId) {
        List<Chat> chats = chatService.findChatsByOrder(orderId);
        return chatMapper.toResponseList(chats);
    }

    @GetMapping("/buyer/{buyerId}")
    public List<ChatResponse> findBuyerChats(@PathVariable Long buyerId) {
        List<Chat> chats = chatService.findBuyerChats(buyerId);
        return chatMapper.toResponseList(chats);
    }

    @GetMapping("/seller/{sellerId}")
    public List<ChatResponse> findSellerChats(@PathVariable Long sellerId) {
        List<Chat> chats = chatService.findSellerChats(sellerId);
        return chatMapper.toResponseList(chats);
    }

    @GetMapping("/{chatId}/messages")
    public List<ChatMessageResponse> findMessagesByChat(@PathVariable Long chatId) {
        List<ChatMessage> messages = chatService.findMessagesByChat(chatId);
        return chatMapper.toMessageResponseList(messages);
    }

    @GetMapping("/messages/sender/{senderId}")
    public List<ChatMessageResponse> findMessagesBySender(@PathVariable Long senderId) {
        List<ChatMessage> messages = chatService.findMessagesBySender(senderId);
        return chatMapper.toMessageResponseList(messages);
    }

    @PostMapping("/messages")
    public ChatMessageResponse sendMessage(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateChatMessageByCurrentUserRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        ChatMessage message = chatService.sendMessageForCurrentUser(currentUser, request);
        return chatMapper.toMessageResponse(message);
    }

    @PatchMapping("/messages/{messageId}/content")
    public void updateMessageContent(
            HttpServletRequest httpRequest,
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageContentRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        chatService.updateMessageContentForCurrentUser(currentUser, messageId, request.content());
    }

    @PatchMapping("/messages/{messageId}/image")
    public void updateMessageImage(
            HttpServletRequest httpRequest,
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateMessageImageRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        chatService.updateMessageImageForCurrentUser(currentUser, messageId, request.imageUrl());
    }
}