package com.example.bankcards.util.mappers;


import com.example.bankcards.dto.adminFuncs.UserRequest;
import com.example.bankcards.dto.adminFuncs.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static com.example.bankcards.entity.Role.*;


@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapToRoles")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "password", source = "password", qualifiedByName = "mapEncodePassword")

    User toEntity(UserRequest dto, @Context PasswordEncoder encoder, @Context RoleRepository repository);

    @Named("mapToRoles")
    default List<Role> mapRoles(String[] roleNames, @Context RoleRepository repository) {
        return repository.findAllByNameIn(Arrays.stream(roleNames).map(RoleName::valueOf).toList());
    }

    @Named("mapEncodePassword")
    default String mapPassword(String password, @Context PasswordEncoder encoder) {
        return encoder.encode(password);
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "hash", source = "password")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapToRoleNames")
    UserResponse toDto(User entity);


    @Named("mapToRoleNames")
    default List<String> mapRoles(List<Role> roles) {
        return roles.stream().map(role -> role.getName().toString()).toList();
    }



}
