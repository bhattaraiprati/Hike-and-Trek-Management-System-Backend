package com.example.treksathi.specification;

import com.example.treksathi.enums.EventStatus;
import com.example.treksathi.model.Event;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> getEvents(EventStatus status, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by Status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            } else {
                // By default, exclude DELETED events if status is null or not specified
                predicates.add(criteriaBuilder.notEqual(root.get("status"), EventStatus.DELETED));
            }

            // Search Filter
            if (search != null && !search.isEmpty()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate title = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchLike);
                Predicate location = criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), searchLike);
                Predicate organizerName = criteriaBuilder
                        .like(criteriaBuilder.lower(root.get("organizer").get("organization_name")), searchLike);

                predicates.add(criteriaBuilder.or(title, location, organizerName));
            }

            query.orderBy(criteriaBuilder.desc(root.get("date")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
