package org.example.repository;

import org.example.domain.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "chat",
            "sender"
    })
    List<ChatMessage> findAll();

    @EntityGraph(attributePaths = {
            "chat",
            "sender"
    })
    List<ChatMessage> findByChat_Id(Long chatId);

    @EntityGraph(attributePaths = {
            "chat",
            "sender"
    })
    List<ChatMessage> findBySender_Id(Long senderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ChatMessage m
            set m.content = :content
            where m.id = :id
            """)
    int updateContentById(@Param("id") Long id,
                          @Param("content") String content);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ChatMessage m
            set m.imageUrl = :imageUrl
            where m.id = :id
            """)
    int updateImageUrlById(@Param("id") Long id,
                           @Param("imageUrl") String imageUrl);
}