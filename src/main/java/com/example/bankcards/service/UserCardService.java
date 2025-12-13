package com.example.bankcards.service;


import com.example.bankcards.dto.userFuncs.CardFilter;
import com.example.bankcards.dto.userFuncs.CardSearchRequest;
import com.example.bankcards.dto.userFuncs.CardDataUserResponse;

import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundExcepion;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardSpecifications;
import com.example.bankcards.util.mappers.CardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static com.example.bankcards.entity.Card.*;

@Service
public class UserCardService {
    @Autowired
    CardRepository cardRepository;

    @Autowired
    CardMapper cardMapper;




    public CardDataUserResponse getBalance(Long cardId) {

        var card =  cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundExcepion("карты с таким id не существует"));
        return cardMapper.toUserDto(card);
    }
    @Transactional
    public void requestBlock(Long cardId) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundExcepion(cardId));
        if (card.getStatus() != CardStatus.ACTIVE) throw new CardNotActiveException();
        cardRepository.updateStatusById(cardId, CardStatus.BLOCKED);
    }

    public Page<CardDataUserResponse> getCards(Long id,
                                               CardFilter filter,
                                               Pageable pageable) {


        Specification<Card> spec = buildSpecification(id, filter, null);

        return cardRepository.findAll(spec,pageable)
                .map(cardMapper::toUserDto);

    }

    public Page<CardDataUserResponse> searchCardsWithNumber(Long id, CardSearchRequest searchRequest, Pageable pageable) {

//        log.info("POST card search for user {}: number={}, status={}",
//                userId, maskedNumber, request.getStatus());

        Specification<Card> spec = buildSpecification(id, convertToFilter(searchRequest), searchRequest.searchNumber());

        return cardRepository.findAll(spec, pageable)
                .map(cardMapper::toUserDto);
    }

    private Specification<Card> buildSpecification(Long userId, CardFilter filter, String cardNumber) {
        Specification<Card> spec = Specification.where(CardSpecifications.byUserId(userId));

        if (StringUtils.hasText(cardNumber)) {
            spec = spec.and(CardSpecifications.byCardNumberEndsWith(cardNumber)
                    .or(CardSpecifications.byCardNumberStartsWith(cardNumber)));
        }

        if (filter.status() != null) {
            spec = spec.and(CardSpecifications.byStatus(filter.status()));
        }

        if (filter.expiryFrom() != null || filter.expiryTo() != null) {
            spec = spec.and(CardSpecifications.expiryDateBetween(
                    filter.expiryFrom(), filter.expiryTo()
            ));
        }

        if (filter.balanceFrom() != null || filter.balanceTo() != null) {
            spec = spec.and(CardSpecifications.balanceBetween(
                    filter.balanceFrom(), filter.balanceTo()
            ));
        }

        return spec;
    }
    private CardFilter convertToFilter(CardSearchRequest request) {
        return CardFilter.builder()
                .status(request.status())
                .expiryFrom(request.expiryFrom())
                .expiryTo(request.expiryTo())
                .balanceFrom(request.balanceFrom())
                .balanceTo(request.balanceTo())
                .build();
    }
}
