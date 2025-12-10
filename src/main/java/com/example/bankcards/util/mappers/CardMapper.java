package com.example.bankcards.util.mappers;


import com.example.bankcards.dto.admin.CardDataAdminRequest;
import com.example.bankcards.dto.admin.CardDataAdminResponse;
import com.example.bankcards.dto.user.CardDataUserResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cardNum", source = "cardNum")
    @Mapping(target = "activeTill", source = "activeTill")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "owner", source = "ownerId", qualifiedByName = "mapToUser")
    @Mapping(target = "status", ignore = true)
    Card toEntity(CardDataAdminRequest dto, @Context UserRepository repository);

    @Named("mapToUser")
    default User mapOwnerId(Long ownerid, @Context UserRepository repository) {
        return repository.findById(ownerid)
                .orElseThrow(() -> new UserNotFoundException("*маппинг* пользователя с таким id не существует"));

    }


    @Mapping(target = "cardNum", source = "cardNum", qualifiedByName = "mapToMaskedNum")
    @Mapping(target = "activeTill", source = "activeTill")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "mapToOwnerName")
    @Mapping(target = "status", source = "status")
    CardDataAdminResponse toAdminDto(Card entity);

    @Mapping(target = "cardNum", source = "cardNum", qualifiedByName = "mapToMaskedNum")
    @Mapping(target = "activeTill", source = "activeTill")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "balance", source = "balance")
    CardDataUserResponse toUserDto(Card entity);


    @Named("mapToMaskedNum")
    default String mapCardNum(String cardNum) {
        String last4Digits = cardNum.substring(cardNum.length() - 4, cardNum.length());
        return "**** **** **** " + last4Digits;
    }

    @Named("mapToOwnerName")
    default String mapCardNum(User owner) {
        return String.format("id = %d, %s",owner.getId(), owner.getName());
    }
}
