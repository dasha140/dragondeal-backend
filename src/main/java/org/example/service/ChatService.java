package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.Chat;
import org.example.domain.ChatMessage;
import org.example.dto.request.CreateChatMessageRequest;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppUserRepository;
import org.example.repository.ChatMessageRepository;
import org.example.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.example.dto.request.CreateChatMessageByCurrentUserRequest;
import org.example.domain.enums.UserRole;
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<Chat> findAllChats() {
        return chatRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Chat findChatWithMessages(Long chatId) {
        return chatRepository.findWithMessagesById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found: " + chatId));
    }

    @Transactional(readOnly = true)
    public List<Chat> findChatsByOrder(Long orderId) {
        return chatRepository.findByOrder_Id(orderId);
    }

    @Transactional(readOnly = true)
    public List<Chat> findBuyerChats(Long buyerId) {
        return chatRepository.findByBuyer_Id(buyerId);
    }

    @Transactional(readOnly = true)
    public List<Chat> findSellerChats(Long sellerId) {
        return chatRepository.findBySeller_Id(sellerId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> findMessagesByChat(Long chatId) {
        return chatMessageRepository.findByChat_Id(chatId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> findMessagesBySender(Long senderId) {
        return chatMessageRepository.findBySender_Id(senderId);
    }

    @Transactional
    public ChatMessage sendMessage(CreateChatMessageRequest request) {
        Chat chat = chatRepository.findWithMessagesById(request.chatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found: " + request.chatId()));

        AppUser sender = appUserRepository.findById(request.senderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found: " + request.senderId()));

        boolean isBuyer = chat.getBuyer().getId().equals(sender.getId());
        boolean isSeller = chat.getSeller().getId().equals(sender.getId());

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("Sender is not a participant of this chat");
        }

        boolean contentBlank = request.content() == null || request.content().isBlank();
        boolean imageBlank = request.imageUrl() == null || request.imageUrl().isBlank();

        if (contentBlank && imageBlank) {
            throw new IllegalArgumentException("Message must contain content or image");
        }

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(contentBlank ? "" : request.content())
                .imageUrl(imageBlank ? "" : request.imageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(message);
    }

    @Transactional
    public ChatMessage sendMessageForCurrentUser(
            AppUser currentUser,
            CreateChatMessageByCurrentUserRequest request
    ) {
        Chat chat = chatRepository.findWithMessagesById(request.chatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found: " + request.chatId()));

        boolean isBuyer = chat.getBuyer().getId().equals(currentUser.getId());
        boolean isSeller = chat.getSeller().getId().equals(currentUser.getId());

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("Only chat participants can send messages");
        }

        boolean contentBlank = request.content() == null || request.content().isBlank();
        boolean imageBlank = request.imageUrl() == null || request.imageUrl().isBlank();

        if (contentBlank && imageBlank) {
            throw new IllegalArgumentException("Message must contain content or image");
        }

        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(currentUser)
                .content(contentBlank ? "" : request.content())
                .imageUrl(imageBlank ? "" : request.imageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(message);
    }

    @Transactional
    public void updateMessageContent(Long messageId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }

        int updatedRows = chatMessageRepository.updateContentById(
                messageId,
                content
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Message not found: " + messageId);
        }
    }

    @Transactional(readOnly = true)
    public List<Chat> findAllChatsForAdmin(AppUser currentUser) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can view all chats");
        }

        return chatRepository.findAll();
    }

    @Transactional
    public void updateMessageImage(Long messageId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image url is required");
        }

        int updatedRows = chatMessageRepository.updateImageUrlById(
                messageId,
                imageUrl
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Message not found: " + messageId);
        }
    }

    @Transactional
    public void updateMessageContentForCurrentUser(AppUser currentUser, Long messageId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only message sender can update this message");
        }

        message.setContent(content);
        chatMessageRepository.save(message);
    }

    @Transactional
    public void updateMessageImageForCurrentUser(AppUser currentUser, Long messageId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image url is required");
        }

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only message sender can update this message");
        }

        message.setImageUrl(imageUrl);
        chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Chat> findMyChats(AppUser currentUser) {
        return chatRepository.findByBuyer_IdOrSeller_Id(
                currentUser.getId(),
                currentUser.getId()
        );
    }

    @Transactional(readOnly = true)
    public Chat findChatWithMessagesForCurrentUser(AppUser currentUser, Long chatId) {
        Chat chat = chatRepository.findWithMessagesById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found: " + chatId));

        boolean isBuyer = chat.getBuyer().getId().equals(currentUser.getId());
        boolean isSeller = chat.getSeller().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isBuyer && !isSeller && !isAdmin) {
            throw new IllegalArgumentException("Only chat participants can view this chat");
        }

        return chat;
    }
}