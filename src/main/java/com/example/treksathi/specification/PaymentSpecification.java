package com.example.treksathi.specification;

import com.example.treksathi.enums.PaymentMethod;
import com.example.treksathi.enums.PaymentStatus;
import com.example.treksathi.model.Payments;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    public static Specification<Payments> getPayments(String status, String method, String search, Integer organizerId,
            LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), PaymentStatus.valueOf(status)));
            }

            if (StringUtils.hasText(method) && !"ALL".equalsIgnoreCase(method)) {
                predicates.add(criteriaBuilder.equal(root.get("method"), PaymentMethod.valueOf(method)));
            }

            if (organizerId != null) {
                predicates.add(criteriaBuilder
                        .equal(root.get("eventRegistration").get("event").get("organizer").get("id"), organizerId));
            }

            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate transactionUuid = criteriaBuilder.like(criteriaBuilder.lower(root.get("transactionUuid")),
                        searchLike);

                // Join for User
                Predicate userName = criteriaBuilder
                        .like(criteriaBuilder.lower(root.get("eventRegistration").get("user").get("name")), searchLike);
                Predicate userEmail = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("eventRegistration").get("user").get("email")), searchLike);
                Predicate organizerName = criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("eventRegistration").get("event").get("organizer").get("organization_name")),
                        searchLike);
                // Join for Event
                Predicate eventTitle = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("eventRegistration").get("event").get("title")), searchLike);

                predicates.add(criteriaBuilder.or(transactionUuid, userName, userEmail, eventTitle, organizerName));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }

            // Order by transactionDate desc
            query.orderBy(criteriaBuilder.desc(root.get("transactionDate")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
