package com.example.bankcards.security.service;


import com.example.bankcards.dto.auth.AuthenticationRequest;
import com.example.bankcards.dto.auth.AuthenticationResponse;
import com.example.bankcards.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager manager;

    public AuthenticationResponse getUser(AuthenticationRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.name(),request.password());
        Authentication authentication = manager.authenticate(token);
        String jwt = jwtService.generateToken(((User) authentication.getPrincipal()));
        return new AuthenticationResponse(jwt);

    }


}
