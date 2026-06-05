package org.example.repository;

import org.example.domain.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByName(String name);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = {
            "listingList",
            "listingList.seller",
            "listingList.category",
            "listingList.imageList"
    })
    Optional<Game> findWithListingsById(Long id);

    @EntityGraph(attributePaths = {
            "listingList",
            "listingList.seller",
            "listingList.category",
            "listingList.imageList"
    })
    List<Game> findByNameContainingIgnoreCase(String name);
}