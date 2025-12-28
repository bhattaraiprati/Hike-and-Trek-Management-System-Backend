package com.example.treksathi.repository;


import com.example.treksathi.model.Favourites;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourites, Integer> {

    // Check if user has favourited an event
    boolean existsByUserIdAndEventId(Integer userId, Integer eventId);

    // Find specific favourite
    Optional<Favourites> findByUserIdAndEventId(Integer userId, Integer eventId);

    // Get all favourites for a user
    Page<Favourites> findByUserIdOrderByAddedAtDesc(Integer userId, Pageable pageable);

    // Get all favourites for a user (no pagination)
    List<Favourites> findByUserId(Integer userId);

    // Count user's favourites
    Long countByUserId(Integer userId);

    // Delete by user and event
    void deleteByUserIdAndEventId(Integer userId, Integer eventId);

    // Get event IDs that user has favourited
    @Query("SELECT f.event.id FROM Favourites f WHERE f.user.id = :userId")
    List<Integer> findEventIdsByUserId(@Param("userId") Integer userId);

    // Custom query to get favourites with event details
    @Query("SELECT f FROM Favourites f " +
            "JOIN FETCH f.event e " +
            "JOIN FETCH e.organizer o " +
            "WHERE f.user.id = :userId " +
            "ORDER BY f.addedAt DESC")
    List<Favourites> findByUserIdWithEventDetails(@Param("userId") Integer userId);
}
