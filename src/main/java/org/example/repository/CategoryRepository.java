package org.example.repository;

import org.example.domain.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = {
            "listingList",
            "listingList.seller",
            "listingList.game",
            "listingList.imageList"
    })
    Optional<Category> findWithListingsById(Long id);

    @EntityGraph(attributePaths = {
            "listingList",
            "listingList.seller",
            "listingList.game",
            "listingList.imageList"
    })
    List<Category> findByNameContainingIgnoreCase(String name);
}