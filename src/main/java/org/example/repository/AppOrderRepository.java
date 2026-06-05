package org.example.repository;

import org.example.domain.AppOrder;
import org.example.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppOrderRepository extends JpaRepository<AppOrder, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "buyer",
            "seller",
            "listing",
            "listing.category",
            "listing.game"
    })
    List<AppOrder> findAll();

    @EntityGraph(attributePaths = {
            "buyer",
            "seller",
            "listing",
            "listing.category",
            "listing.game"
    })
    Optional<AppOrder> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
            "buyer",
            "seller",
            "listing",
            "listing.category",
            "listing.game"
    })
    List<AppOrder> findByBuyer_Id(Long buyerId);

    @EntityGraph(attributePaths = {
            "buyer",
            "seller",
            "listing",
            "listing.category",
            "listing.game"
    })
    List<AppOrder> findBySeller_Id(Long sellerId);

    @EntityGraph(attributePaths = {
            "buyer",
            "seller",
            "listing",
            "listing.category",
            "listing.game"
    })
    List<AppOrder> findByStatus(OrderStatus status);

    boolean existsByPaymentTxHash(String paymentTxHash);

    long countBySeller_IdAndStatus(Long sellerId, OrderStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppOrder o
            set o.paymentTxHash = :paymentTxHash,
                o.status = :status,
                o.paidAt = :paidAt
            where o.id = :id
            """)
    int markAsPaid(
            @Param("id") Long id,
            @Param("paymentTxHash") String paymentTxHash,
            @Param("status") OrderStatus status,
            @Param("paidAt") LocalDateTime paidAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppOrder o
            set o.sellerMarkedDoneAt = :sellerMarkedDoneAt,
                o.status = :status
            where o.id = :id
            """)
    int markSellerDone(
            @Param("id") Long id,
            @Param("sellerMarkedDoneAt") LocalDateTime sellerMarkedDoneAt,
            @Param("status") OrderStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppOrder o
            set o.releaseTxHash = :releaseTxHash,
                o.completedAt = :completedAt,
                o.status = :status
            where o.id = :id
            """)
    int completeOrder(
            @Param("id") Long id,
            @Param("releaseTxHash") String releaseTxHash,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("status") OrderStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update AppOrder o
            set o.status = :status
            where o.id = :id
            """)
    int updateStatusById(
            @Param("id") Long id,
            @Param("status") OrderStatus status
    );
}