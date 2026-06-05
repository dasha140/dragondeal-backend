package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.WalletProvider;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "wallet_address", nullable = false, length = 255)
    private String walletAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "network", nullable = false, length = 50)
    private BlockchainNetwork network;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 100)
    private WalletProvider provider;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}