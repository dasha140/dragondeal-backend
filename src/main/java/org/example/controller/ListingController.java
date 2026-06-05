package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.Listing;
import org.example.dto.request.CreateListingByCurrentUserRequest;
import org.example.dto.request.UpdateListingRequest;
import org.example.dto.response.ListingResponse;
import org.example.mapper.ListingMapper;
import org.example.service.CurrentUserService;
import org.example.service.ListingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final ListingMapper listingMapper;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<ListingResponse> findAllListings() {
        List<Listing> listings = listingService.findAllListings();
        return listingMapper.toResponseList(listings);
    }

    @GetMapping("/seller/me")
    public List<ListingResponse> findMyListings(HttpServletRequest httpRequest) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        List<Listing> listings = listingService.findSellerListings(currentUser.getId());
        return listingMapper.toResponseList(listings);
    }

    @GetMapping("/seller/{sellerId}")
    public List<ListingResponse> findActiveListingsBySeller(@PathVariable Long sellerId) {
        List<Listing> listings = listingService.findActiveListingsBySeller(sellerId);
        return listingMapper.toResponseList(listings);
    }

    @GetMapping("/{listingId}")
    public ListingResponse findListingById(@PathVariable Long listingId) {
        Listing listing = listingService.findListingById(listingId);
        return listingMapper.toResponse(listing);
    }

    @PostMapping
    public ListingResponse createListing(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateListingByCurrentUserRequest request
    ) {
        AppUser seller = currentUserService.getCurrentUser(httpRequest);
        Listing listing = listingService.createListingForCurrentUser(seller, request);
        return listingMapper.toResponse(listing);
    }

    @PatchMapping("/{listingId}")
    public ListingResponse updateListing(
            HttpServletRequest httpRequest,
            @PathVariable Long listingId,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        Listing listing = listingService.updateListingForCurrentUser(currentUser, listingId, request);
        return listingMapper.toResponse(listing);
    }
    @GetMapping("/active")
    public List<ListingResponse> findActiveListings() {
        List<Listing> listings = listingService.findActiveListings();
        return listingMapper.toResponseList(listings);
    }


    @GetMapping("/category/{categoryId}/active")
    public List<ListingResponse> findActiveListingsByCategory(@PathVariable Long categoryId) {
        List<Listing> listings = listingService.findActiveListingsByCategory(categoryId);
        return listingMapper.toResponseList(listings);
    }

    @GetMapping("/game/{gameId}/active")
    public List<ListingResponse> findActiveListingsByGame(@PathVariable Long gameId) {
        List<Listing> listings = listingService.findActiveListingsByGame(gameId);
        return listingMapper.toResponseList(listings);
    }

    @GetMapping("/price/{minPrice}/{maxPrice}/active")
    public List<ListingResponse> findActiveListingsByPrice(
            @PathVariable BigDecimal minPrice,
            @PathVariable BigDecimal maxPrice
    ) {
        List<Listing> listings = listingService.findActiveListingsByPrice(minPrice, maxPrice);
        return listingMapper.toResponseList(listings);
    }

    @GetMapping("/search/{title}")
    public List<ListingResponse> searchActiveListingsByTitle(@PathVariable String title) {
        List<Listing> listings = listingService.searchActiveListingsByTitle(title);
        return listingMapper.toResponseList(listings);
    }

    @PatchMapping("/{listingId}/deactivate")
    public void deactivateListing(
            HttpServletRequest httpRequest,
            @PathVariable Long listingId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        listingService.deactivateListingForCurrentUser(currentUser, listingId);
    }
    @PatchMapping("/{listingId}/block")
    public void blockListing(
            HttpServletRequest httpRequest,
            @PathVariable Long listingId
    ) {
        AppUser currentUser = currentUserService.getCurrentUser(httpRequest);
        listingService.blockListingForAdmin(currentUser, listingId);
    }
}