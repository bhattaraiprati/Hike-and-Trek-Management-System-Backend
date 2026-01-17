package com.example.treksathi.specification;

import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.Role;
import com.example.treksathi.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> getUsers(String role, String status, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(role) && !"ALL".equalsIgnoreCase(role)) {
                predicates.add(criteriaBuilder.equal(root.get("role"), Role.valueOf(role)));
            }

            if (StringUtils.hasText(status) && !"ALL".equalsIgnoreCase(status)) {
                predicates.add(criteriaBuilder.equal(root.get("status"), AccountStatus.valueOf(status)));
            }

            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchLike);
                Predicate emailPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchLike);
                Predicate phonePredicate = criteriaBuilder.like(root.get("phone"), searchLike);
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, phonePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
