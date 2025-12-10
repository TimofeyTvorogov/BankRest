package com.example.bankcards.security;

import com.example.bankcards.dto.signup.SignupRequest;
import com.example.bankcards.dto.signup.SignupResponse;
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
public class SignupController {

    @Autowired
    SignupService signupService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signupUser(@RequestBody @Validated SignupRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(signupService.createUser(request));
    }
}
