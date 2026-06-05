package org.example.repository;

import org.example.domain.WalletTransaction;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.TransactionStatus;
import org.example.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "order",
            "user"
    })
    List<WalletTransaction> findAll();

    @EntityGraph(attributePaths = {
            "order",
            "user"
    })
    Optional<WalletTransaction> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
            "order",
            "user"
    })
    List<WalletTransaction> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {
            "order",
            "user"
    })
    List<WalletTransaction> findByUser_Id(Long userId);

    Optional<WalletTransaction> findByTxHash(String txHash);

    boolean existsByTxHash(String txHash);

    List<WalletTransaction> findByStatus(TransactionStatus status);

    List<WalletTransaction> findByType(TransactionType type);

    List<WalletTransaction> findByNetwork(BlockchainNetwork network);

    List<WalletTransaction> findByCurrency(String currency);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update WalletTransaction t
            set t.status = :status
            where t.id = :id
            """)
    int updateStatusById(@Param("id") Long id,
                         @Param("status") TransactionStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update WalletTransaction t
            set t.txHash = :txHash,
                t.status = :status
            where t.id = :id
            """)
    int updateTxHashAndStatusById(@Param("id") Long id,
                                  @Param("txHash") String txHash,
                                  @Param("status") TransactionStatus status);
}