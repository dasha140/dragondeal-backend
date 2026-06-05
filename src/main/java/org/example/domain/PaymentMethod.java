package org.example.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.enums.CryptoCurrency;
import org.example.domain.enums.CryptoNetwork;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_method")
@Getter
@Setter
@NoArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CryptoCurrency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CryptoNetwork network;

    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Column(name = "token_contract", nullable = false, length = 128)
    private String tokenContract;

    @Column(name = "escrow_contract", nullable = false, length = 128)
    private String escrowContract;

    @Column(nullable = false)
    private Integer decimals;

    @Column(name = "min_amount", nullable = false, precision = 19, scale = 8)
    private BigDecimal minAmount;

    @Column(name = "is_enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (enabled == null) {
            enabled = false;
        }

        if (sortOrder == null) {
            sortOrder = 100;
        }
    }
}