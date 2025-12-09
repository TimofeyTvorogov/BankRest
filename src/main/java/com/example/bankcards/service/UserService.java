package com.example.bankcards.service;


import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mappers.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public List<UserResponse> findAllUsers() {
        userRepository.findAll().stream().map(mapper::toDto).forEach(userResponse -> log.info(userResponse.toString()));
        return userRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException("no user with id " + id);
        userRepository.deleteById(id);
    }

    public UserResponse createUser(UserRequest dto) {
        var userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();
        var user = mapper.toEntityCustom(dto, encoder, userRole);
        return mapper.toDto(userRepository.save(user));
    }

//    public UserResponse updateUser(UserRequest user) {
//        if (!userRepository.existsByName(user.getName())) {
//            var newUser = userRepository.save(user);
//        }
//    }
}


