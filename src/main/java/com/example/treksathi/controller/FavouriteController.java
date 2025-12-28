package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IFavouriteService;
import com.example.treksathi.dto.favourites.AddFavouriteRequest;
import com.example.treksathi.dto.favourites.FavouriteResponse;
import com.example.treksathi.dto.favourites.FavouritesPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hiker/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final IFavouriteService favouriteService;

    // Add to favourites
    @PostMapping
    public ResponseEntity<FavouriteResponse> addToFavourites(
            Authentication authentication,
            @RequestBody AddFavouriteRequest request) {
        String email = authentication.getName();
        FavouriteResponse response = favouriteService.addToFavourites(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Remove from favourites
    @DeleteMapping("/{eventId}")
    public ResponseEntity<FavouriteResponse> removeFromFavourites(
            Authentication authentication,
            @PathVariable Integer eventId) {
        String email = authentication.getName();
        FavouriteResponse response = favouriteService.removeFromFavourites(email, eventId);
        return ResponseEntity.ok(response);
    }

    // Toggle favourite (smart add/remove)
    @PostMapping("/toggle/{eventId}")
    public ResponseEntity<FavouriteResponse> toggleFavourite(
            Authentication authentication,
            @PathVariable Integer eventId) {
        String email = authentication.getName();
        FavouriteResponse response = favouriteService.toggleFavourite(email, eventId);
        return ResponseEntity.ok(response);
    }

    // Check if event is favourited
    @GetMapping("/check/{eventId}")
    public ResponseEntity<Boolean> isFavourite(
            Authentication authentication,
            @PathVariable Integer eventId) {
        String email = authentication.getName();
        Boolean isFavourite = favouriteService.isFavourite(email, eventId);
        return ResponseEntity.ok(isFavourite);
    }

    // Get all favourites (paginated)
    @GetMapping
    public ResponseEntity<FavouritesPageResponse> getUserFavourites(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        FavouritesPageResponse response = favouriteService.getUserFavourites(email, pageable);
        return ResponseEntity.ok(response);
    }

    // Get favourite event IDs (for quick lookup)
    @GetMapping("/event-ids")
    public ResponseEntity<List<Integer>> getFavouriteEventIds(Authentication authentication) {
        String email = authentication.getName();
        List<Integer> eventIds = favouriteService.getUserFavouriteEventIds(email);
        return ResponseEntity.ok(eventIds);
    }

    // Get favourites count
    @GetMapping("/count")
    public ResponseEntity<Long> getFavouritesCount(Authentication authentication) {
        String email = authentication.getName();
        Long count = favouriteService.getFavouritesCount(email);
        return ResponseEntity.ok(count);
    }

}
