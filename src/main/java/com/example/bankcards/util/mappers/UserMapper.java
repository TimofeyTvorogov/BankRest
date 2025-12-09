package com.example.bankcards.util.mappers;


import com.example.bankcards.dto.SignupRequest;
import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRequest dto);
    default User toEntityCustom(UserRequest request, PasswordEncoder encoder, Role userRole) {
        User user = toEntity(request);
        user.setPassword(encoder.encode(request.password()));
        user.setRoles(List.of(userRole));
        return user;
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "hash", source = "password")
    @Mapping(target = "roles", source = "roles")
    UserResponse toDto(User entity);

    default List<String> mapRoles(List<Role> roles) {
        return roles.stream().map(role -> role.getName().toString()).toList();
    }



}
