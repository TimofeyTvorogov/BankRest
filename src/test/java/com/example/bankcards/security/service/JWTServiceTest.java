package com.example.bankcards.security.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    private String validSecretBase64;
    private SecretKey secretKey;
    private UserDetails testUserDetails;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Генерируем тестовый секрет в base64
        validSecretBase64 = Base64.getEncoder().encodeToString(
                "test-secret-key-for-jwt-testing-purposes-only".getBytes()
        );

        // Устанавливаем значения через reflection
        ReflectionTestUtils.setField(jwtService, "SECRET_BASE64_ENCODED", validSecretBase64);
        ReflectionTestUtils.setField(jwtService, "tokenLifetimeMillis", 3600000); // 1 час
        ReflectionTestUtils.setField(jwtService, "iss", "test-issuer");

        // Создаем секретный ключ
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(validSecretBase64));

        // Создаем тестового пользователя
        List<Role> roles = new ArrayList<>();
        roles.add(Role.builder().name(Role.RoleName.ROLE_USER).build());
        roles.add(Role.builder().name(Role.RoleName.ROLE_ADMIN).build());

        testUser = User.builder()
                .id(100L)
                .name("testuser")
                .password("encodedPassword123")
                .roles(roles)
                .cards(new ArrayList<>())
                .build();

        testUserDetails = testUser;
    }

    @Test
    @DisplayName("Когда генерируем токен для UserDetails, тогда возвращается валидный JWT токен")
    void generateToken_withValidUserDetails_returnsJwtToken() {

        String token = jwtService.generateToken(testUserDetails);


        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();


        String[] tokenParts = token.split("\\.");
        assertThat(tokenParts).hasSize(3);


        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Когда извлекаем username из валидного токена, тогда возвращается правильное имя")
    void extractUsername_fromValidToken_returnsUsername() throws Exception {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Когда извлекаем expiration из валидного токена, тогда возвращается дата истечения")
    void extractExpiration_fromValidToken_returnsExpirationDate() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        Date expiration = jwtService.extractExpiration(token);

        // Assert
        assertThat(expiration).isAfter(new Date());

        // Проверяем, что срок истечения примерно через 1 час (допуск ± 5 секунд)
        long expectedTime = System.currentTimeMillis() + 3600000L;
        long actualTime = expiration.getTime();
        assertThat(Math.abs(actualTime - expectedTime)).isLessThan(5000L);
    }

    @Test
    @DisplayName("Когда проверяем валидный токен для правильного пользователя, тогда возвращается true")
    void isTokenValid_validTokenForCorrectUser_returnsTrue() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUserDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Когда проверяем токен для другого пользователя, тогда возвращается false")
    void isTokenValid_tokenForDifferentUser_returnsFalse() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        UserDetails differentUser = User.builder()
                .name("differentuser")
                .password("password")
                .roles(new ArrayList<>())
                .build();

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Когда проверяем истекший токен, тогда возвращается false")
    void isTokenValid_expiredToken_returnsFalse() throws Exception {
        // Arrange - создаем токен с истекшим сроком
        ReflectionTestUtils.setField(jwtService, "tokenLifetimeMillis", 0); // Прошедшее время
        String expiredToken = jwtService.generateToken(testUserDetails);

        // Возвращаем нормальное время
        Thread.sleep(2000);

        // Act
        assertThatThrownBy(()-> jwtService.isTokenValid(expiredToken,testUserDetails)).isInstanceOf(JwtException.class);

    }

    @Test
    @DisplayName("Когда передаем null токен, тогда выбрасывается исключение")
    void extractUsername_nullToken_throwsException() {
        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUsername(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CharSequence cannot be null or empty");
    }

    @Test
    @DisplayName("Когда передаем пустой токен, тогда выбрасывается исключение")
    void extractUsername_emptyToken_throwsException() {
        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUsername(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CharSequence cannot be null or empty");
    }

    @Test
    @DisplayName("Когда передаем невалидный токен, тогда выбрасывается исключение")
    void extractUsername_invalidToken_throwsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Когда передаем подделанный токен, тогда выбрасывается исключение")
    void extractUsername_tamperedToken_throwsException() {
        // Arrange
        String validToken = jwtService.generateToken(testUserDetails);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("signature");
    }


    @Test
    @DisplayName("Когда issuer установлен, он включается в claims токена")
    void generateToken_includesIssuerInClaims() throws Exception {
        // Arrange
        String customIssuer = "my-custom-issuer";
        ReflectionTestUtils.setField(jwtService, "iss", customIssuer);

        // Act
        String token = jwtService.generateToken(testUserDetails);

        // Используем reflection для доступа к private методу getClaims
        Method getClaimsMethod = JWTService.class.getDeclaredMethod("getClaims", String.class);
        getClaimsMethod.setAccessible(true);
        Claims claims = (Claims) getClaimsMethod.invoke(jwtService, token);

        // Assert
        assertThat(claims.getIssuer()).isEqualTo(customIssuer);
    }

    @Test
    @DisplayName("Когда генерируем токены для разных пользователей, они разные")
    void generateToken_differentUsers_differentTokens() {
        // Arrange
        UserDetails user1 = User.builder().name("user1").password("pass1").build();
        UserDetails user2 = User.builder().name("user2").password("pass2").build();

        // Act
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertThat(token1).isNotEqualTo(token2);

        String username1 = jwtService.extractUsername(token1);
        String username2 = jwtService.extractUsername(token2);

        assertThat(username1).isEqualTo("user1");
        assertThat(username2).isEqualTo("user2");
    }

    @Test
    @DisplayName("Когда генерируем токены в разное время, они разные")
    void generateToken_differentTimes_differentTokens() throws InterruptedException {
        // Act
        String token1 = jwtService.generateToken(testUserDetails);
        Thread.sleep(1000); // Ждем 100ms
        String token2 = jwtService.generateToken(testUserDetails);

        // Assert
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Когда проверяем токен с null UserDetails, выбрасывается исключение")
    void isTokenValid_nullUserDetails_throwsException() {
        // Arrange
        String token = jwtService.generateToken(testUserDetails);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.isTokenValid(token, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Когда secret не base64 encoded, выбрасывается исключение")
    void getKey_invalidBase64Secret_throwsException() {
        // Arrange
        String invalidBase64 = "not-valid-base64-!@#$";
        ReflectionTestUtils.setField(jwtService, "SECRET_BASE64_ENCODED", invalidBase64);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.generateToken(testUserDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base64");
    }

    @Test
    @DisplayName("Когда token lifetime равен 0, токен немедленно истекает")
    void generateToken_zeroLifetime_tokenExpiresImmediately() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "tokenLifetimeMillis", 0);
        String token = jwtService.generateToken(testUserDetails);

        // Возвращаем нормальное значение
        ReflectionTestUtils.setField(jwtService, "tokenLifetimeMillis", 3600000);

        // Даем немного времени, чтобы убедиться, что токен истек
        Thread.sleep(10);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUserDetails);

        // Assert
        assertThat(isValid).isFalse();
    }


    @Test
    @DisplayName("Интеграция: полный цикл генерации и валидации токена работает корректно")
    void integration_generateAndValidateToken_worksCorrectly() {
        // Act - генерируем токен
        String token = jwtService.generateToken(testUserDetails);

        // Assert - проверяем все аспекты
        assertThat(token).isNotNull();

        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("testuser");

        Date expiration = jwtService.extractExpiration(token);
        assertThat(expiration).isAfter(new Date());

        boolean isValid = jwtService.isTokenValid(token, testUserDetails);
        assertThat(isValid).isTrue();

        // Проверяем для другого пользователя
        UserDetails wrongUser = User.builder().name("wronguser").password("pass").build();
        boolean isValidForWrongUser = jwtService.isTokenValid(token, wrongUser);
        assertThat(isValidForWrongUser).isFalse();
    }

    @Test
    @DisplayName("Когда issuer не установлен, используется значение по умолчанию")
    void generateToken_defaultIssuer_whenIssuerNotSet() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "iss", null);

        // Act
        String token = jwtService.generateToken(testUserDetails);

        // Получаем claims через reflection
        Method getClaimsMethod = JWTService.class.getDeclaredMethod("getClaims", String.class);
        getClaimsMethod.setAccessible(true);
        Claims claims = (Claims) getClaimsMethod.invoke(jwtService, token);

        // Assert
        assertThat(claims.getIssuer()).isNull(); // или значение по умолчанию из аннотации
    }

    @Test
    @DisplayName("Когда secret слишком короткий, генерируется исключение")
    void getKey_veryShortSecret_throwsException() {
        // Arrange
        String shortSecretBase64 = Base64.getEncoder().encodeToString("a".getBytes());
        ReflectionTestUtils.setField(jwtService, "SECRET_BASE64_ENCODED", shortSecretBase64);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.generateToken(testUserDetails))
                .isInstanceOf(WeakKeyException.class);
    }
}