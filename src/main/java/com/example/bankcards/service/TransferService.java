package com.example.bankcards.service;


import com.example.bankcards.dto.userFuncs.transfer.TransferRequest;
import com.example.bankcards.dto.userFuncs.transfer.TransferResponse;
import com.example.bankcards.dto.userFuncs.transfer.TransferSummary;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mappers.CardMapper;
import com.example.bankcards.util.mappers.TransferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.example.bankcards.entity.Card.*;

@Service
public class TransferService {
    @Autowired
    TransferMapper transferMapper;
    @Autowired
    CardMapper cardMapper;

    @Autowired
    TransferRepository transferRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CardRepository cardRepository;


    @Transactional
    public TransferResponse createTransfer(TransferRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Card fromCard = cardRepository.findByIdAndOwnerId(request.fromCardId(), userId)
                .orElseThrow(() -> new CardNotFoundExcepion(request.fromCardId()));
        String fromCardNumMasked = cardMapper.mapCardNumFromEntity(fromCard);
        if (fromCard.getStatus() != CardStatus.ACTIVE)
            throw new CardNotActiveException(fromCardNumMasked);

        Card toCard = cardRepository.findByIdAndOwnerId(request.toCardId(), userId)
                .orElseThrow(() -> new CardNotFoundExcepion(request.toCardId()));
        if (toCard.getStatus() != CardStatus.ACTIVE)
            throw new CardNotActiveException(cardMapper.mapCardNumFromEntity(toCard));

        if (request.fromCardId() == request.toCardId())
            throw new IllegalStateException("перевод должен осуществляться на разные карты");

        if (fromCard.getBalance().compareTo(request.amount()) < 0)
            throw new InsufficientFundsException(fromCardNumMasked, fromCard.getBalance(),request.amount());

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalStateException("сумма перевода должна быть положительным числом");

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));
        cardRepository.saveAll(List.of(fromCard,toCard));

        Transfer transfer = transferMapper.toEntity(request,cardRepository, user);
        transferRepository.save(transfer);

        return transferMapper.toResponse(transfer);

    }

    public Page<TransferSummary> getTransferHistory(Pageable pageable, Long userId) {
        return transferRepository.findAllByInitiatedById(pageable, userId)
                .map(transferMapper::toSummary);
    }
}
