package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppOrder;
import org.example.domain.Chat;
import org.example.domain.ChatMessage;
import org.example.domain.Dispute;
import org.example.domain.Listing;
import org.example.repository.AppOrderRepository;
import org.example.repository.ChatRepository;
import org.example.repository.DisputeRepository;
import org.example.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketDemoService {

    private final ListingRepository listingRepository;
    private final AppOrderRepository appOrderRepository;
    private final ChatRepository chatRepository;
    private final DisputeRepository disputeRepository;

    @Transactional(readOnly = true)
    public void runDemo() {
        printListings();
        printOrders();
        printChats();
        printDisputes();
    }

    private void printListings() {
        List<Listing> listings = listingRepository.findAll();

        System.out.println("========== LISTINGS ==========");

        for (Listing listing : listings) {
            System.out.println("ID: " + listing.getId());
            System.out.println("Title: " + listing.getTitle());
            System.out.println("Seller: " + listing.getSeller().getUsername());
            System.out.println("Category: " + listing.getCategory().getName());
            System.out.println("Game: " + listing.getGame().getName());
            System.out.println("Price: " + listing.getPrice() + " " + listing.getCurrency());
            System.out.println("Network: " + listing.getNetwork());
            System.out.println("Delivery type: " + listing.getDeliveryType());
            System.out.println("Status: " + listing.getStatus());
            System.out.println("Images count: " + listing.getImageList().size());
            System.out.println("------------------------------");
        }
    }

    private void printOrders() {
        List<AppOrder> orders = appOrderRepository.findAll();

        System.out.println("========== ORDERS ==========");

        for (AppOrder order : orders) {
            System.out.println("ID: " + order.getId());
            System.out.println("Buyer: " + order.getBuyer().getUsername());
            System.out.println("Seller: " + order.getSeller().getUsername());
            System.out.println("Listing: " + order.getListing().getTitle());
            System.out.println("Total: " + order.getTotalPrice() + " " + order.getCurrency());
            System.out.println("Network: " + order.getNetwork());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Payment tx: " + order.getPaymentTxHash());
            System.out.println("------------------------------");
        }
    }

    private void printChats() {
        List<Chat> chats = chatRepository.findAll();

        System.out.println("========== CHATS ==========");

        for (Chat chat : chats) {
            System.out.println("Chat ID: " + chat.getId());
            System.out.println("Order ID: " + chat.getOrder().getId());
            System.out.println("Buyer: " + chat.getBuyer().getUsername());
            System.out.println("Seller: " + chat.getSeller().getUsername());

            for (ChatMessage message : chat.getMessageList()) {
                System.out.println(message.getSender().getUsername() + ": " + message.getContent());
            }

            System.out.println("------------------------------");
        }
    }

    private void printDisputes() {
        List<Dispute> disputes = disputeRepository.findAll();

        System.out.println("========== DISPUTES ==========");

        for (Dispute dispute : disputes) {
            System.out.println("Dispute ID: " + dispute.getId());
            System.out.println("Order ID: " + dispute.getOrder().getId());
            System.out.println("Opened by: " + dispute.getOpenedBy().getUsername());
            System.out.println("Status: " + dispute.getStatus());
            System.out.println("Reason: " + dispute.getReason());
            System.out.println("Evidence count: " + dispute.getEvidenceList().size());
            System.out.println("------------------------------");
        }
    }
}