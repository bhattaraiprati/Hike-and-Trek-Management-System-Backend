package com.example.treksathi.Interfaces;

import com.example.treksathi.dto.favourites.AddFavouriteRequest;
import com.example.treksathi.dto.favourites.FavouriteResponse;
import com.example.treksathi.dto.favourites.FavouritesPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IFavouriteService {

    // Add event to favourites
    FavouriteResponse addToFavourites(String email, AddFavouriteRequest request);

    // Remove from favourites
    FavouriteResponse removeFromFavourites(String email, Integer eventId);

    // Toggle favourite (add if not exists, remove if exists)
    FavouriteResponse toggleFavourite(String email, Integer eventId);

    // Check if event is favourited by user
    Boolean isFavourite(String email, Integer eventId);

    // Get all favourites for user (paginated)
    FavouritesPageResponse getUserFavourites(String email, Pageable pageable);

    // Get all favourite event IDs (for quick lookup)
    List<Integer> getUserFavouriteEventIds(String email);

    // Get favourites count
    Long getFavouritesCount(String email);

    // Update favourite notes
}
