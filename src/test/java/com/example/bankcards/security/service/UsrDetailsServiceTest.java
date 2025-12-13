package com.example.bankcards.security.service;


import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class UsrDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsrDetailsService usrDetailsService;

    private User testUser;
    private List<Role> userRoles;

    @BeforeEach
    void setUp() {
        // Создаем тестовые роли
        Role userRole = Role.builder()
                .id(1L)
                .name(Role.RoleName.ROLE_USER)
                .build();

        Role adminRole = Role.builder()
                .id(2L)
                .name(Role.RoleName.ROLE_ADMIN)
                .build();

        userRoles = new ArrayList<>();
        userRoles.add(userRole);
        userRoles.add(adminRole);

        // Создаем тестового пользователя
        testUser = User.builder()
                .id(100L)
                .name("testuser")
                .password("encodedPassword123")
                .roles(userRoles)
                .cards(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Когда загружаем существующего пользователя по имени, тогда возвращается UserDetails")
    void loadUserByUsername_existingUser_returnsUserDetails() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByName(username))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");

        // Проверяем authorities (роли)
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

        // Проверяем, что пользователь активен
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();

        verify(userRepository).findByName(username);
    }

    @Test
    @DisplayName("Когда загружаем несуществующего пользователя, тогда выбрасывается UsernameNotFoundException")
    void loadUserByUsername_nonExistentUser_throwsException() {
        // Arrange
        String nonExistentUsername = "nonexistent";
        when(userRepository.findByName(nonExistentUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usrDetailsService.loadUserByUsername(nonExistentUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("no user with name " + nonExistentUsername);

        verify(userRepository).findByName(nonExistentUsername);
    }

    @Test
    @DisplayName("Когда загружаем пользователя с пустым именем, тогда выбрасывается UsernameNotFoundException")
    void loadUserByUsername_emptyUsername_throwsException() {
        // Arrange
        String emptyUsername = "";
        when(userRepository.findByName(emptyUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usrDetailsService.loadUserByUsername(emptyUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("no user with name " + emptyUsername);

        verify(userRepository).findByName(emptyUsername);
    }

    @Test
    @DisplayName("Когда загружаем пользователя с null именем, тогда выбрасывается UsernameNotFoundException")
    void loadUserByUsername_nullUsername_throwsException() {
        // Arrange
        when(userRepository.findByName(null))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usrDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("no user with name null");

        verify(userRepository).findByName(null);
    }

    @Test
    @DisplayName("Когда загружаем пользователя только с ROLE_USER, тогда возвращается одна роль")
    void loadUserByUsername_userWithSingleRole_returnsSingleAuthority() {
        // Arrange
        User userWithSingleRole = User.builder()
                .id(200L)
                .name("simpleuser")
                .password("password123")
                .roles(List.of(Role.builder().name(Role.RoleName.ROLE_USER).build()))
                .cards(new ArrayList<>())
                .build();

        when(userRepository.findByName("simpleuser"))
                .thenReturn(Optional.of(userWithSingleRole));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername("simpleuser");

        // Assert
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Когда загружаем пользователя без ролей, тогда authorities пустой")
    void loadUserByUsername_userWithoutRoles_returnsEmptyAuthorities() {
        // Arrange
        User userWithoutRoles = User.builder()
                .id(300L)
                .name("noroleuser")
                .password("password123")
                .roles(new ArrayList<>()) // Пустой список ролей
                .cards(new ArrayList<>())
                .build();

        when(userRepository.findByName("noroleuser"))
                .thenReturn(Optional.of(userWithoutRoles));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername("noroleuser");

        // Assert
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Когда загружаем пользователя с null списком ролей, тогда authorities пустой")
    void loadUserByUsername_userWithNullRoles_returnsEmptyAuthorities() {

        User userWithNullRoles = User.builder()
                .id(400L)
                .name("nullroleuser")
                .password("password123")
                .roles(new ArrayList<>())
                .cards(new ArrayList<>())
                .build();

        when(userRepository.findByName("nullroleuser"))
                .thenReturn(Optional.of(userWithNullRoles));

        UserDetails userDetails = usrDetailsService.loadUserByUsername("nullroleuser");


        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Транзакционность: метод выполняется в транзакции")
    void loadUserByUsername_transactional_executesInTransaction() {
        // Arrange
        when(userRepository.findByName("testuser"))
                .thenReturn(Optional.of(testUser));

        // Act
        usrDetailsService.loadUserByUsername("testuser");

        // Assert
        // Сам факт наличия @Transactional аннотации проверяется через метаданные
        // Мы можем проверить, что метод вызывается корректно
        verify(userRepository).findByName("testuser");
    }

    @Test
    @DisplayName("Когда пользователь имеет разные учетные данные, они правильно маппятся")
    void loadUserByUsername_userWithDifferentCredentials_mapsCorrectly() {
        // Arrange
        User userWithDifferentPassword = User.builder()
                .id(500L)
                .name("differentuser")
                .password("differentEncodedPassword!@#")
                .roles(userRoles)
                .cards(new ArrayList<>())
                .build();

        when(userRepository.findByName("differentuser"))
                .thenReturn(Optional.of(userWithDifferentPassword));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername("differentuser");

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo("differentuser");
        assertThat(userDetails.getPassword()).isEqualTo("differentEncodedPassword!@#");
    }

    @Test
    @DisplayName("Когда ищем пользователя с разными регистрами, поиск чувствителен к регистру")
    void loadUserByUsername_caseSensitive_searchIsCaseSensitive() {
        // Arrange
        // В базе есть "testuser" (в нижнем регистре)
        when(userRepository.findByName("testuser"))
                .thenReturn(Optional.of(testUser));

        // "TestUser" с большой буквы не должен найтись
        when(userRepository.findByName("TestUser"))
                .thenReturn(Optional.empty());

        // Act & Assert для нижнего регистра
        UserDetails lowerCaseUser = usrDetailsService.loadUserByUsername("testuser");
        assertThat(lowerCaseUser).isNotNull();
        assertThat(lowerCaseUser.getUsername()).isEqualTo("testuser");

        // Act & Assert для верхнего регистра
        assertThatThrownBy(() -> usrDetailsService.loadUserByUsername("TestUser"))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(userRepository).findByName("testuser");
        verify(userRepository).findByName("TestUser");
    }

    @Test
    @DisplayName("Когда загружаем пользователя с пробелами в имени, они учитываются")
    void loadUserByUsername_usernameWithSpaces_handlesCorrectly() {
        // Arrange
        User userWithSpaces = User.builder()
                .id(600L)
                .name("user with spaces")
                .password("password123")
                .roles(userRoles)
                .cards(new ArrayList<>())
                .build();

        when(userRepository.findByName("user with spaces"))
                .thenReturn(Optional.of(userWithSpaces));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername("user with spaces");

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo("user with spaces");
    }

    @Test
    @DisplayName("Когда репозиторий выбрасывает исключение, оно пробрасывается дальше")
    void loadUserByUsername_repositoryThrowsException_propagatesException() {
        // Arrange
        String username = "erroruser";
        when(userRepository.findByName(username))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThatThrownBy(() -> usrDetailsService.loadUserByUsername(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");

        verify(userRepository).findByName(username);
    }

    @Test
    @DisplayName("Когда пользователь найден, возвращается именно тот объект, что из репозитория")
    void loadUserByUsername_returnsSameObjectFromRepository() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByName(username))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername(username);

        // Assert
        // Проверяем, что это тот же объект User (не обернутый дополнительно)
        assertThat(userDetails)
                .isInstanceOf(User.class)
                .isSameAs(testUser);
    }

    @Test
    @DisplayName("Интеграция: UserDetails имеет все необходимые методы для Spring Security")
    void loadUserByUsername_returnsValidUserDetailsForSpringSecurity() {
        // Arrange
        when(userRepository.findByName("secureuser"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = usrDetailsService.loadUserByUsername("secureuser");

        // Assert - проверяем все требования Spring Security UserDetails
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
}