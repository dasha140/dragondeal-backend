package org.example.repository;

import org.example.domain.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrder_IdAndReviewer_Id(Long orderId, Long reviewerId);

    @Override
    @EntityGraph(attributePaths = {
            "order",
            "reviewer",
            "targetUser"
    })
    List<Review> findAll();

    @EntityGraph(attributePaths = {
            "order",
            "reviewer",
            "targetUser"
    })
    Optional<Review> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
            "order",
            "reviewer",
            "targetUser"
    })
    List<Review> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {
            "order",
            "reviewer",
            "targetUser"
    })
    List<Review> findByReviewer_Id(Long reviewerId);

    @EntityGraph(attributePaths = {
            "order",
            "reviewer",
            "targetUser"
    })
    List<Review> findByTargetUser_Id(Long targetUserId);

    @EntityGraph(attributePaths = {
            "order",
            "reviewer",
            "targetUser"
    })
    List<Review> findByTargetUser_IdOrderByCreatedAtDesc(Long targetUserId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Review r
            set r.rating = :rating,
                r.comment = :comment
            where r.id = :id
            """)
    int updateReviewById(@Param("id") Long id,
                         @Param("rating") Integer rating,
                         @Param("comment") String comment);

    @Query("""
        select coalesce(avg(r.rating), 0.0)
        from Review r
        where r.targetUser.id = :targetUserId
        """)
    Double getAverageRatingByTargetUserId(@Param("targetUserId") Long targetUserId);
}