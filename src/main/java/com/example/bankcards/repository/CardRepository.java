package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.Card.*;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    Optional<Card> findByCardNum(String cardNum);

    void deleteByCardNum(String cardNum);

    @Modifying
    @Query("UPDATE Card c SET c.status= ?2 WHERE c.id = ?1")
    void updateStatusById(Long id, CardStatus status);

    boolean existsByCardNum(String cardNum);

    Optional<Card> findByIdAndOwnerId(Long id, Long ownerId);

}
