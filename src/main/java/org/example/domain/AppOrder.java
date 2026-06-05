package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_order")
public class AppOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private AppUser buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private AppUser seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", nullable = false, length = 20)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "network", nullable = false, length = 50)
    private BlockchainNetwork network;

    @Column(name = "escrow_contract", length = 255)
    private String escrowContract;

    @Column(name = "escrow_deal_id", length = 255)
    private String escrowDealId;

    @Column(name = "payment_tx_hash", length = 255)
    private String paymentTxHash;

    @Column(name = "release_tx_hash", length = 255)
    private String releaseTxHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "seller_marked_done_at")
    private LocalDateTime sellerMarkedDoneAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    private List<WalletTransaction> transactionList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    private List<Chat> chatList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    private List<Review> reviewList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
    private List<Dispute> disputeList;
}