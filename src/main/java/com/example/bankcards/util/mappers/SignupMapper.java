package com.example.bankcards.util.mappers;


import com.example.bankcards.dto.signup.SignupRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


@Mapper(componentModel = "spring")
public interface SignupMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "password", ignore = true)
    User toEntity(SignupRequest request);


    default User toEntityCustom(SignupRequest request, PasswordEncoder encoder, Role userRole) {
        User user = toEntity(request);
        user.setPassword(encoder.encode(request.password()));
        user.setRoles(List.of(userRole));
        return user;
    }
}