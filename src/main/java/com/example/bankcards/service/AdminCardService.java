package com.example.bankcards.service;


import com.example.bankcards.dto.adminFuncs.CardDataAdminRequest;
import com.example.bankcards.dto.adminFuncs.CardDataAdminResponse;
import com.example.bankcards.exception.CardHasExpiredException;
import com.example.bankcards.exception.CardNotFoundExcepion;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mappers.CardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.Card.CardStatus;

@Service
public class AdminCardService {
    @Autowired
    CardRepository cardRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    CardMapper cardMapper;

    @Transactional
    public CardDataAdminResponse addCard(CardDataAdminRequest dto) {
        var owner = userRepository.findById(dto.ownerId())
                .orElseThrow(() -> new UserNotFoundException(dto.ownerId()));
        var createdCard = cardMapper.toEntity(dto, userRepository);
        owner.addCard(createdCard);
        return cardMapper.toAdminDto(cardRepository.saveAndFlush(createdCard));
    }



    @Transactional
    public void blockCard(Long cardId) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundExcepion(cardId));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardHasExpiredException();
        }

        cardRepository.updateStatusById(cardId, CardStatus.BLOCKED);
    }

    @Transactional
    public void activateCard(Long cardId) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundExcepion(cardId));

        if (card.getStatus() == CardStatus.ACTIVE) return;
        if (!card.getActiveTill().isAfter(LocalDate.now())) {
           throw new CardHasExpiredException();
        }
        cardRepository.updateStatusById(cardId, CardStatus.ACTIVE);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId))
            throw new CardNotFoundExcepion(cardId);
        cardRepository.deleteById(cardId);
    }

    public Page<CardDataAdminResponse> getCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(cardMapper::toAdminDto);
    }

    public CardDataAdminResponse getCardById(Long cardId) {
        return cardMapper.toAdminDto(cardRepository.findById(cardId).orElseThrow(() ->
                new CardNotFoundExcepion(cardId)));


    }



}
