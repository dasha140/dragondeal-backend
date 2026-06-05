package org.example.mapper;

import org.example.domain.Chat;
import org.example.domain.ChatMessage;
import org.example.dto.response.ChatMessageResponse;
import org.example.dto.response.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {

    public ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getContent(),
                message.getImageUrl(),
                message.getCreatedAt()
        );
    }

    public List<ChatMessageResponse> toMessageResponseList(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::toMessageResponse)
                .toList();
    }

    public ChatResponse toResponse(Chat chat) {
        List<ChatMessageResponse> messages = chat.getMessageList()
                .stream()
                .map(this::toMessageResponse)
                .toList();

        return new ChatResponse(
                chat.getId(),
                chat.getOrder().getId(),
                chat.getBuyer().getId(),
                chat.getBuyer().getUsername(),
                chat.getSeller().getId(),
                chat.getSeller().getUsername(),
                chat.getCreatedAt(),
                messages
        );
    }

    public List<ChatResponse> toResponseList(List<Chat> chats) {
        return chats.stream()
                .map(this::toResponse)
                .toList();
    }
}