package org.example.repository;

import org.example.domain.Chat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "order",
            "buyer",
            "seller",
            "messageList",
            "messageList.sender"
    })
    List<Chat> findAll();

    @EntityGraph(attributePaths = {
            "order",
            "buyer",
            "seller",
            "messageList",
            "messageList.sender"
    })
    Optional<Chat> findWithMessagesById(Long id);

    @EntityGraph(attributePaths = {
            "order",
            "buyer",
            "seller",
            "messageList",
            "messageList.sender"
    })
    List<Chat> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {
            "order",
            "buyer",
            "seller"
    })
    List<Chat> findByBuyer_Id(Long buyerId);

    @EntityGraph(attributePaths = {
            "order",
            "buyer",
            "seller"
    })
    List<Chat> findBySeller_Id(Long sellerId);

    @EntityGraph(attributePaths = {
            "order",
            "buyer",
            "seller",
            "messageList",
            "messageList.sender"
    })
    List<Chat> findByBuyer_IdOrSeller_Id(Long buyerId, Long sellerId);
}