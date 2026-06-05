package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.AppOrder;
import org.example.domain.AppUser;
import org.example.domain.Chat;
import org.example.domain.Listing;
import org.example.domain.enums.ListingStatus;
import org.example.domain.enums.OrderStatus;
import org.example.dto.request.CreateOrderRequest;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AppOrderRepository;
import org.example.repository.AppUserRepository;
import org.example.repository.ChatRepository;
import org.example.repository.ListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final AppOrderRepository appOrderRepository;
    private final AppUserRepository appUserRepository;
    private final ListingRepository listingRepository;
    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<AppOrder> findAllOrders() {
        return appOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppOrder findOrderById(Long orderId) {
        return appOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<AppOrder> findBuyerOrders(Long buyerId) {
        return appOrderRepository.findByBuyer_Id(buyerId);
    }

    @Transactional(readOnly = true)
    public List<AppOrder> findSellerOrders(Long sellerId) {
        return appOrderRepository.findBySeller_Id(sellerId);
    }

    @Transactional(readOnly = true)
    public List<AppOrder> findOrdersByStatus(OrderStatus status) {
        return appOrderRepository.findByStatus(status);
    }

    @Transactional
    public void markOrderAsPaidForCurrentUser(AppUser currentUser, Long orderId, String paymentTxHash) {
        validatePaymentTxHash(paymentTxHash);

        String trimmedHash = paymentTxHash.trim();

        if (appOrderRepository.existsByPaymentTxHash(trimmedHash)) {
            throw new IllegalArgumentException("Payment transaction hash already used");
        }

        AppOrder order = appOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only order buyer can mark this order as paid");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalArgumentException("Only CREATED order can be marked as PAID");
        }

        int updatedRows = appOrderRepository.markAsPaid(
                orderId,
                trimmedHash,
                OrderStatus.PAID,
                LocalDateTime.now()
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
    }

    @Transactional
    public AppOrder createOrderForCurrentUser(AppUser buyer, Long listingId) {
        Listing listing = listingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Listing is not active: " + listing.getId());
        }

        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Seller cannot buy own listing");
        }

        LocalDateTime now = LocalDateTime.now();

        AppOrder order = AppOrder.builder()
                .buyer(buyer)
                .seller(listing.getSeller())
                .listing(listing)
                .totalPrice(listing.getPrice())
                .currency(listing.getCurrency())
                .network(listing.getNetwork())
                .status(OrderStatus.CREATED)
                .escrowContract("")
                .escrowDealId("")
                .paymentTxHash("")
                .releaseTxHash("")
                .createdAt(now)
                .build();

        AppOrder savedOrder = appOrderRepository.save(order);

        createChatForOrder(savedOrder, buyer, listing.getSeller(), now);

        return savedOrder;
    }

    @Transactional
    public AppOrder createOrder(CreateOrderRequest request) {
        AppUser buyer = appUserRepository.findById(request.buyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + request.buyerId()));

        Listing listing = listingRepository.findWithDetailsById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + request.listingId()));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Listing is not active: " + listing.getId());
        }

        if (listing.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Seller cannot buy own listing");
        }

        LocalDateTime now = LocalDateTime.now();

        AppOrder order = AppOrder.builder()
                .buyer(buyer)
                .seller(listing.getSeller())
                .listing(listing)
                .totalPrice(listing.getPrice())
                .currency(listing.getCurrency())
                .network(listing.getNetwork())
                .status(OrderStatus.CREATED)
                .escrowContract("")
                .escrowDealId("")
                .paymentTxHash("")
                .releaseTxHash("")
                .createdAt(now)
                .build();

        AppOrder savedOrder = appOrderRepository.save(order);

        createChatForOrder(savedOrder, buyer, listing.getSeller(), now);

        return savedOrder;
    }

    @Transactional
    public void markSellerDone(Long orderId) {
        AppOrder order = appOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Only PAID order can be marked as SELLER_DONE");
        }

        int updatedRows = appOrderRepository.markSellerDone(
                orderId,
                LocalDateTime.now(),
                OrderStatus.SELLER_DONE
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }
    }

    @Transactional
    public void completeOrder(Long orderId, String releaseTxHash) {
        if (releaseTxHash == null || releaseTxHash.isBlank()) {
            throw new IllegalArgumentException("Release transaction hash is required");
        }

        AppOrder order = appOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.SELLER_DONE) {
            throw new IllegalArgumentException("Only SELLER_DONE order can be completed");
        }

        int updatedRows = appOrderRepository.completeOrder(
                orderId,
                releaseTxHash.trim(),
                LocalDateTime.now(),
                OrderStatus.COMPLETED
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        Listing listing = order.getListing();
        listing.setStatus(ListingStatus.SOLD);
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        AppOrder order = appOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalArgumentException("Only CREATED order can be cancelled");
        }

        int updatedRows = appOrderRepository.updateStatusById(
                orderId,
                OrderStatus.CANCELLED
        );

        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Order not found: " + orderId);
        }

        Listing listing = order.getListing();
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    private void createChatForOrder(
            AppOrder order,
            AppUser buyer,
            AppUser seller,
            LocalDateTime createdAt
    ) {
        Chat chat = Chat.builder()
                .order(order)
                .buyer(buyer)
                .seller(seller)
                .createdAt(createdAt)
                .build();

        chatRepository.save(chat);
    }

    private void validatePaymentTxHash(String paymentTxHash) {
        if (paymentTxHash == null || paymentTxHash.isBlank()) {
            throw new IllegalArgumentException("Payment transaction hash is required");
        }

        String trimmedHash = paymentTxHash.trim();

        if (!trimmedHash.matches("^0x[a-fA-F0-9]{64}$")) {
            throw new IllegalArgumentException("Invalid payment transaction hash format");
        }
    }
}