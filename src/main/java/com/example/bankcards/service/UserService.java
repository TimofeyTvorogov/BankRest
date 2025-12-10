package com.example.bankcards.service;


import com.example.bankcards.dto.admin.UserRequest;
import com.example.bankcards.dto.admin.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mappers.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper mapper;

    @Autowired
    RoleRepository roleRepository;

    public UserResponse findUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("no user with id " + id));
        return mapper.toDto(user);
    }

    public List<UserResponse> findAllUsers(int pageNum, int size) {
        return userRepository.findAll(PageRequest.of(pageNum,size)).stream().map(mapper::toDto).toList();
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException("no user with id " + id);
        userRepository.deleteById(id);
    }

    //todo test
    public UserResponse createUser(UserRequest dto) {
        var user = mapper.toEntity(dto,encoder,roleRepository);
        return mapper.toDto(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UserRequest dto) {
        var oldUser = userRepository.findById(id);
        if (!oldUser.isPresent()) {
            return createUser(dto);
        }
        User toUpdateUser = oldUser.get();
        toUpdateUser.setName(dto.name());
        toUpdateUser.setPassword(encoder.encode(dto.password()));
        toUpdateUser.setRoles(mapper.mapRoles(dto.roles(),roleRepository));
        return mapper.toDto(userRepository.save(toUpdateUser));
    }
}


