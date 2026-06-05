package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.enums.PaymentIntentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_intent")
@Getter
@Setter
@NoArgsConstructor
public class PaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private AppOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(name = "amount_atomic", nullable = false, length = 100)
    private String amountAtomic;

    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Column(name = "token_contract", nullable = false, length = 128)
    private String tokenContract;

    @Column(name = "escrow_contract", nullable = false, length = 128)
    private String escrowContract;

    @Column(name = "escrow_deal_id", length = 100)
    private String escrowDealId;

    @Column(name = "payment_tx_hash", length = 128)
    private String paymentTxHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentIntentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = PaymentIntentStatus.CREATED;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (expiresAt == null) {
            expiresAt = createdAt.plusMinutes(30);
        }
    }
}