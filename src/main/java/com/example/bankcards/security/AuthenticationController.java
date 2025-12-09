package com.example.bankcards.security;


import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.dto.SignupRequest;
import com.example.bankcards.dto.SignupResponse;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;


    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> postUser(@RequestBody @Validated SignupRequest request) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(authenticationService.createUser(request));
        }
        catch (UserAlreadyExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new SignupResponse("пользователь с таким именем уже существует"));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> getUser(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.getUser(request));
    }
}
