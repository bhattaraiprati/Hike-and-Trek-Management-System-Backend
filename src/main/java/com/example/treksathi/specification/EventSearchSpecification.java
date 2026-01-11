package com.example.treksathi.specification;

import com.example.treksathi.dto.search.SearchCriteria;
import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.Organizer;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EventSearchSpecification {

    public static Specification<Event> searchEvents(SearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only active events
            predicates.add(criteriaBuilder.equal(root.get("status"), EventStatus.ACTIVE));

            // Text search in title, description, location
            if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getQuery().toLowerCase() + "%";

                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        searchPattern
                );

                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        searchPattern
                );

                Predicate locationPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        searchPattern
                );

                predicates.add(criteriaBuilder.or(
                        titlePredicate,
                        descriptionPredicate,
                        locationPredicate
                ));
            }

            // Difficulty level filter
            if (criteria.getDifficultyLevel() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("difficultyLevel"),
                        criteria.getDifficultyLevel()
                ));
            }

            // Price range filter
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        criteria.getMinPrice()
                ));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        criteria.getMaxPrice()
                ));
            }

            // Date range filter
            if (criteria.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("date"),
                        criteria.getStartDate()
                ));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("date"),
                        criteria.getEndDate()
                ));
            }

            // Duration filter
            if (criteria.getMinDuration() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("durationDays"),
                        criteria.getMinDuration()
                ));
            }
            if (criteria.getMaxDuration() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("durationDays"),
                        criteria.getMaxDuration()
                ));
            }

            // Location filter (exact match)
            if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + criteria.getLocation().toLowerCase() + "%"
                ));
            }

            // Organizer name filter
            if (criteria.getOrganizerName() != null && !criteria.getOrganizerName().trim().isEmpty()) {
                Join<Event, Organizer> organizerJoin = root.join("organizer", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(organizerJoin.get("organization_name")),
                        "%" + criteria.getOrganizerName().toLowerCase() + "%"
                ));
            }

            // Organizer ID filter
            if (criteria.getOrganizerId() != null) {
                Join<Event, Organizer> organizerJoin = root.join("organizer", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                        organizerJoin.get("id"),
                        criteria.getOrganizerId()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
