package org.example.repository;

import org.example.domain.Listing;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.DeliveryType;
import org.example.domain.enums.ListingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findBySeller_IdAndStatus(Long sellerId, ListingStatus status);

    @Override
    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findAll();

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    Optional<Listing> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findByStatus(ListingStatus status);

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findBySeller_Id(Long sellerId);

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findByCategory_IdAndStatus(Long categoryId, ListingStatus status);

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findByGame_IdAndStatus(Long gameId, ListingStatus status);

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findByPriceBetweenAndStatus(BigDecimal minPrice,
                                              BigDecimal maxPrice,
                                              ListingStatus status);

    @EntityGraph(attributePaths = {
            "seller",
            "category",
            "game",
            "imageList"
    })
    List<Listing> findByTitleContainingIgnoreCaseAndStatus(String title,
                                                           ListingStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Listing l
            set l.status = :status
            where l.id = :id
            """)
    int updateStatusById(@Param("id") Long id,
                         @Param("status") ListingStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Listing l
            set l.price = :price
            where l.id = :id
            """)
    int updatePriceById(@Param("id") Long id,
                        @Param("price") BigDecimal price);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Listing l
            set l.title = :title,
                l.description = :description,
                l.price = :price,
                l.currency = :currency,
                l.network = :network,
                l.deliveryType = :deliveryType,
                l.status = :status
            where l.id = :id
            """)
    int updateMainInfoById(@Param("id") Long id,
                           @Param("title") String title,
                           @Param("description") String description,
                           @Param("price") BigDecimal price,
                           @Param("currency") String currency,
                           @Param("network") BlockchainNetwork network,
                           @Param("deliveryType") DeliveryType deliveryType,
                           @Param("status") ListingStatus status);
}