package org.example.repository;

import org.example.domain.ListingImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "listing",
            "listing.seller",
            "listing.category",
            "listing.game"
    })
    List<ListingImage> findAll();

    @EntityGraph(attributePaths = {
            "listing",
            "listing.seller",
            "listing.category",
            "listing.game"
    })
    List<ListingImage> findByListing_Id(Long listingId);
}