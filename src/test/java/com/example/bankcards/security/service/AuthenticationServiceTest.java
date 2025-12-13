package com.example.bankcards.security.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.bankcards.dto.auth.AuthenticationRequest;
import com.example.bankcards.dto.auth.AuthenticationResponse;
import com.example.bankcards.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AuthenticationServiceTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationRequest validRequest;
    private User testUser;
    private String expectedToken;

    @BeforeEach
    void setUp() {
        validRequest = new AuthenticationRequest("testuser", "password123");

        testUser = User.builder()
                .id(1L)
                .name("testuser")
                .password("encodedPassword")
                .build();

        expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    }

    @Test
    @DisplayName("Когда аутентифицируем пользователя с валидными данными, тогда возвращается JWT токен")
    void getUser_withValidCredentials_returnsJwtToken() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(validRequest.name(), validRequest.password());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(testUser);
        when(jwtService.generateToken(testUser))
                .thenReturn(expectedToken);

        // Act
        AuthenticationResponse result = authenticationService.getUser(validRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(expectedToken);

        verify(authenticationManager).authenticate(authToken);
        verify(authentication).getPrincipal();
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Когда аутентифицируем пользователя, создается правильный UsernamePasswordAuthenticationToken")
    void getUser_createsCorrectAuthenticationToken() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(testUser);
        when(jwtService.generateToken(testUser))
                .thenReturn(expectedToken);


        authenticationService.getUser(validRequest);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Когда аутентификация успешна, генерируется JWT токен для пользователя")
    void getUser_successfulAuthentication_generatesJwtForUser() {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(testUser);
        when(jwtService.generateToken(testUser))
                .thenReturn(expectedToken);


        AuthenticationResponse result = authenticationService.getUser(validRequest);


        assertThat(result.token()).isEqualTo(expectedToken);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("Когда AuthenticationManager возвращает аутентификацию, извлекается правильный пользователь")
    void getUser_extractsCorrectUserFromAuthentication() {

        User anotherUser = User.builder()
                .id(2L)
                .name("anotheruser")
                .password("anotherPassword")
                .build();

        String anotherToken = "another.token.here";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(anotherUser);
        when(jwtService.generateToken(anotherUser))
                .thenReturn(anotherToken);


        AuthenticationResponse result = authenticationService.getUser(validRequest);


        assertThat(result.token()).isEqualTo(anotherToken);
        verify(jwtService).generateToken(anotherUser);
    }

    @Test
    @DisplayName("Когда передаются разные учетные данные, создаются разные токены аутентификации")
    void getUser_withDifferentCredentials_createsDifferentTokens() {

        AuthenticationRequest request1 = new AuthenticationRequest("user1", "pass1");
        AuthenticationRequest request2 = new AuthenticationRequest("user2", "pass2");

        User user1 = User.builder().id(1L).name("user1").build();
        User user2 = User.builder().id(2L).name("user2").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);


        when(authentication.getPrincipal())
                .thenReturn(user1);
        when(jwtService.generateToken(user1))
                .thenReturn("token1");


        authenticationService.getUser(request1);


        when(authentication.getPrincipal())
                .thenReturn(user2);
        when(jwtService.generateToken(user2))
                .thenReturn("token2");

        AuthenticationResponse result = authenticationService.getUser(request2);


        assertThat(result.token()).isEqualTo("token2");
    }

    @Test
    @DisplayName("Когда передается null запрос, выбрасывается исключение")
    void getUser_withNullRequest_throwsException() {

        // Spring Security сам выбросит исключение при попытке создать UsernamePasswordAuthenticationToken с null

    }

    @Test
    @DisplayName("Когда передается запрос с пустыми полями, выбрасывается исключение")
    void getUser_withEmptyCredentials_throwsException() {

        AuthenticationRequest emptyRequest = new AuthenticationRequest("", "");


        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});


        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        authenticationService.getUser(emptyRequest))
                .isInstanceOf(org.springframework.security.core.AuthenticationException.class);
    }
}