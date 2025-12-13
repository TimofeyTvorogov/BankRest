package com.example.bankcards.security.service;

import com.example.bankcards.dto.signup.SignupRequest;
import com.example.bankcards.dto.signup.SignupResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.service.JWTService;
import com.example.bankcards.util.mappers.SignupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    JWTService jwtService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    SignupMapper mapper;

    @Transactional
    public SignupResponse createUser(SignupRequest request) {
        if (userRepository.findByName(request.name()).isPresent()) throw new UserAlreadyExistsException(request.name());
        var userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();

        var user = mapper.toEntityCustom(request,encoder,userRole);

        String jwt = jwtService.generateToken(userRepository.save(user));
        return new SignupResponse(jwt);
    }
}
