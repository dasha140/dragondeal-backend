package org.example.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "firebase_uid", length = 128, unique = true)
    private String firebaseUid;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Wallet> walletList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "seller")
    private List<Listing> listingList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "buyer")
    private List<AppOrder> buyerOrderList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "seller")
    private List<AppOrder> sellerOrderList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<WalletTransaction> transactionList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "buyer")
    private List<Chat> buyerChatList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "seller")
    private List<Chat> sellerChatList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sender")
    private List<ChatMessage> messageList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "reviewer")
    private List<Review> writtenReviewList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "targetUser")
    private List<Review> receivedReviewList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "openedBy")
    private List<Dispute> openedDisputeList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<DisputeEvidence> disputeEvidenceList;
}