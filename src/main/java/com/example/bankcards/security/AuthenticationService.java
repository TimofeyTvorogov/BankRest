package com.example.bankcards.security;


import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.dto.auth.LoginResponse;
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

    public LoginResponse getUser(LoginRequest request) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(request.name(),request.password());
        Authentication authentication = manager.authenticate(token);
        String jwt = jwtService.generateToken(((User) authentication.getPrincipal()));
        return new LoginResponse(jwt);

    }


}
