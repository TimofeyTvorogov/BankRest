package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import static com.example.bankcards.entity.Card.*;

public class CardSpecifications {
    public static Specification<Card> byUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("owner").get("id"), userId);
    }


    public static Specification<Card> byCardNumberEndsWith(String lastDigits) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(lastDigits)) return null;

            return cb.like(root.get("cardNum"), "%" + lastDigits);
        };
    }

    public static Specification<Card> byCardNumberStartsWith(String firstDigits) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(firstDigits)) return null;

            return cb.like(root.get("cardNum"), firstDigits + "%");
        };
    }

    public static Specification<Card> byStatus(CardStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }


    public static Specification<Card> expiryDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expiryDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expiryDate"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Card> balanceBetween(BigDecimal from, BigDecimal to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            List<Predicate> predicates = new ArrayList<>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("balance"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("balance"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
