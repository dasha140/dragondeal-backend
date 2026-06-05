package org.example.service;

import org.example.domain.enums.UserRole;
import org.example.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.domain.AppUser;
import org.example.domain.Category;
import org.example.domain.Game;
import org.example.domain.Listing;
import org.example.domain.ListingImage;
import org.example.domain.enums.ListingStatus;
import org.example.dto.request.CreateListingRequest;
import org.example.repository.AppUserRepository;
import org.example.repository.CategoryRepository;
import org.example.repository.GameRepository;
import org.example.repository.ListingImageRepository;
import org.example.repository.ListingRepository;
import org.example.dto.request.CreateListingByCurrentUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.domain.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.dto.request.UpdateListingRequest;
import org.example.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final AppUserRepository appUserRepository;
    private final CategoryRepository categoryRepository;
    private final GameRepository gameRepository;
    private final ListingImageRepository listingImageRepository;

    @Transactional(readOnly = true)
    public List<Listing> findAllListings() {
        return listingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Listing findListingById(Long listingId) {
        return listingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
    }

    @Transactional(readOnly = true)
    public List<Listing> findActiveListings() {
        return listingRepository.findByStatus(ListingStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Listing> findSellerListings(Long sellerId) {
        return listingRepository.findBySeller_Id(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Listing> findActiveListingsByCategory(Long categoryId) {
        return listingRepository.findByCategory_IdAndStatus(categoryId, ListingStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Listing> findActiveListingsByGame(Long gameId) {
        return listingRepository.findByGame_IdAndStatus(gameId, ListingStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Listing> findActiveListingsByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        return listingRepository.findByPriceBetweenAndStatus(
                minPrice,
                maxPrice,
                ListingStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public List<Listing> searchActiveListingsByTitle(String title) {
        return listingRepository.findByTitleContainingIgnoreCaseAndStatus(
                title,
                ListingStatus.ACTIVE
        );
    }

    @Transactional
    public Listing createListing(CreateListingRequest request) {
        AppUser seller = appUserRepository.findById(request.sellerId())
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + request.sellerId()));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));

        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + request.gameId()));

        LocalDateTime now = LocalDateTime.now();

        Listing listing = Listing.builder()
                .seller(seller)
                .category(category)
                .game(game)
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .currency(request.currency())
                .network(request.network())
                .deliveryType(request.deliveryType())
                .status(ListingStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Listing savedListing = listingRepository.save(listing);

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<ListingImage> images = request.imageUrls()
                    .stream()
                    .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                    .map(imageUrl -> ListingImage.builder()
                            .listing(savedListing)
                            .imageUrl(imageUrl)
                            .createdAt(now)
                            .build())
                    .toList();

            List<ListingImage> savedImages = listingImageRepository.saveAll(images);
            savedListing.setImageList(savedImages);
        } else {
            savedListing.setImageList(List.of());
        }

        return savedListing;
    }

    @Transactional
    public Listing createListingForCurrentUser(AppUser seller, CreateListingByCurrentUserRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.categoryId()));

        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + request.gameId()));

        LocalDateTime now = LocalDateTime.now();

        Listing listing = Listing.builder()
                .seller(seller)
                .category(category)
                .game(game)
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .currency(request.currency())
                .network(request.network())
                .deliveryType(request.deliveryType())
                .status(ListingStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Listing savedListing = listingRepository.save(listing);

        if (request.imageUrls() != null && !request.imageUrls().isEmpty()) {
            List<ListingImage> images = request.imageUrls()
                    .stream()
                    .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                    .map(imageUrl -> ListingImage.builder()
                            .listing(savedListing)
                            .imageUrl(imageUrl)
                            .createdAt(now)
                            .build())
                    .toList();

            List<ListingImage> savedImages = listingImageRepository.saveAll(images);
            savedListing.setImageList(savedImages);
        } else {
            savedListing.setImageList(List.of());
        }

        return savedListing;
    }

    @Transactional
    public Listing updateListing(Long listingId, UpdateListingRequest request) {
        Listing listing = listingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setCurrency(request.currency());
        listing.setNetwork(request.network());
        listing.setDeliveryType(request.deliveryType());
        listing.setUpdatedAt(LocalDateTime.now());

        Listing savedListing = listingRepository.save(listing);

        return listingRepository.findWithDetailsById(savedListing.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + savedListing.getId()));
    }

    @Transactional
    public Listing updateListingForCurrentUser(AppUser currentUser, Long listingId, UpdateListingRequest request) {
        Listing listing = listingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        if (!listing.getSeller().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only listing seller can update this listing");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE listing can be updated");
        }

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setCurrency(request.currency());
        listing.setNetwork(request.network());
        listing.setDeliveryType(request.deliveryType());
        listing.setUpdatedAt(LocalDateTime.now());

        Listing savedListing = listingRepository.save(listing);

        return listingRepository.findWithDetailsById(savedListing.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + savedListing.getId()));
    }

    @Transactional
    public void deactivateListing(Long listingId) {
        int updatedRows = listingRepository.updateStatusById(
                listingId,
                ListingStatus.INACTIVE
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }
    }

    @Transactional
    public void markListingAsSold(Long listingId) {
        int updatedRows = listingRepository.updateStatusById(
                listingId,
                ListingStatus.SOLD
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }
    }

    @Transactional
    public void blockListing(Long listingId) {
        int updatedRows = listingRepository.updateStatusById(
                listingId,
                ListingStatus.BLOCKED
        );

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }
    }

    @Transactional
    public void deactivateListingForCurrentUser(AppUser currentUser, Long listingId) {
        Listing listing = listingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        if (!listing.getSeller().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only listing seller can deactivate this listing");
        }

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE listing can be deactivated");
        }

        listing.setStatus(ListingStatus.INACTIVE);
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    @Transactional
    public void blockListingForAdmin(AppUser currentUser, Long listingId) {
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admin can block listing");
        }

        Listing listing = listingRepository.findWithDetailsById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        listing.setStatus(ListingStatus.BLOCKED);
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public List<Listing> findActiveListingsBySeller(Long sellerId) {
        return listingRepository.findBySeller_IdAndStatus(sellerId, ListingStatus.ACTIVE);
    }

    @Transactional
    public void updateListingPrice(Long listingId, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        int updatedRows = listingRepository.updatePriceById(listingId, price);

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }
    }
}