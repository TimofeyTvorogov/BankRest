package com.example.bankcards.util.mappers;

import com.example.bankcards.dto.userFuncs.transfer.TransferRequest;
import com.example.bankcards.dto.userFuncs.transfer.TransferResponse;
import com.example.bankcards.dto.userFuncs.transfer.TransferSummary;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundExcepion;
import com.example.bankcards.repository.CardRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;



@Mapper(componentModel = "spring",
        uses = {CardMapper.class})
public interface TransferMapper {

    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "amount",)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "status", constant = "COMPLETED")
    @Mapping(target = "fromCard", source = "request.fromCardId", qualifiedByName = "mapToCard")
    @Mapping(target = "toCard", source = "request.toCardId", qualifiedByName = "mapToCard")
    @Mapping(target = "initiatedBy", source = "initiatedBy")

    Transfer toEntity(TransferRequest request,
                      @Context CardRepository cardRepository,
                      User initiatedBy);

    @Named("mapToCard")
    default Card mapToCard(Long cardId, @Context CardRepository cardRepository) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundExcepion("Card with ID " + cardId + " not found"));
    }


    @Mapping(target = "fromCardMasked", source = "fromCard", qualifiedByName = "mapToMaskedNumFromEntity")
    @Mapping(target = "toCardMasked", source = "toCard", qualifiedByName = "mapToMaskedNumFromEntity")
    @Mapping(target = "status", source = "status")
    TransferResponse toResponse(Transfer transfer);

    @Mapping(target = "fromCardMasked", source = "fromCard", qualifiedByName = "mapToMaskedNumFromEntity")
    @Mapping(target = "toCardMasked", source = "toCard", qualifiedByName = "mapToMaskedNumFromEntity")
    @Mapping(target = "status", source = "status")
    TransferSummary toSummary(Transfer transfer);


}