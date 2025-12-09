package com.example.bankcards.security;


import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.SignupRequest;
import com.example.bankcards.dto.SignupResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager manager;

    public SignupResponse createUser(SignupRequest request) {
        if (userRepository.existsByName(request.name())) throw new UserAlreadyExistsException();
        var user = new User();
        user.setName(request.name());
        user.setPassword(encoder.encode(request.password()));
        var userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).orElseThrow();
        user.setRoles(List.of(userRole));

        var savedUser = userRepository.save(user);

        String jwt = jwtService.generateToken(savedUser);
        return new SignupResponse(jwt);
    }

    public LoginResponse getUser(LoginRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.name(),request.password());
        Authentication authentication = manager.authenticate(token);
        String jwt = jwtService.generateToken(((User) authentication.getPrincipal()));
        return new LoginResponse(jwt);

    }


}
