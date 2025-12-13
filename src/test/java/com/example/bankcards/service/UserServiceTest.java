package com.example.bankcards.service;

import com.example.bankcards.dto.adminFuncs.*;

import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.*;
import com.example.bankcards.entity.*;
import com.example.bankcards.util.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.bankcards.entity.Role.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class UserServiceTest {

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest validUserRequest;
    private UserResponse expectedUserResponse;
    private List<Role> userRoles;

    @BeforeEach
    void setUp() {
        userRoles = new ArrayList<>();
        userRoles.add(builder().id(1L).name(RoleName.ROLE_USER).build());

        testUser = User.builder()
                .id(1L)
                .name("testuser")
                .password("encodedPassword")
                .roles(userRoles)
                .cards(new ArrayList<>())
                .build();

        validUserRequest = new UserRequest(
                "testuser",
                "password123",
                List.of("ROLE_USER").toArray(String[]::new)
        );

        expectedUserResponse = new UserResponse(
                1L,
                "testuser",
                "hash",
                List.of("ROLE_USER")
        );
    }

    @Test
    @DisplayName("Когда находим пользователя по существующему ID, тогда возвращается UserResponse")
    void findUserById_existingId_returnsUserResponse() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(mapper.toDto(testUser))
                .thenReturn(expectedUserResponse);


        UserResponse result = userService.findUserById(1L);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
        verify(mapper).toDto(testUser);
    }

    @Test
    @DisplayName("Когда находим пользователя по несуществующему ID, тогда выбрасывается UserNotFoundException")
    void findUserById_nonExistentId_throwsException() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findById(999L);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("Когда получаем всех пользователей с пагинацией, тогда возвращается страница")
    void findAllUsers_withPageable_returnsPage() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAll(pageable))
                .thenReturn(userPage);
        when(mapper.toDto(testUser))
                .thenReturn(expectedUserResponse);


        Page<UserResponse> result = userService.findAllUsers(pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);

        verify(userRepository).findAll(pageable);
        verify(mapper).toDto(testUser);
    }

    @Test
    @DisplayName("Когда удаляем существующего пользователя, тогда пользователь удаляется")
    @Transactional
    void deleteUser_existingUser_deletesUser() {

        when(userRepository.existsById(1L))
                .thenReturn(true);


        userService.deleteUser(1L);


        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Когда удаляем несуществующего пользователя, тогда выбрасывается UserNotFoundException")
    void deleteUser_nonExistentUser_throwsException() {

        when(userRepository.existsById(999L))
                .thenReturn(false);


        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Когда создаем пользователя с валидными данными, тогда пользователь сохраняется")
    @Transactional
    void createUser_withValidData_savesUser() {

        User mappedUser = User.builder()
                .id(null)
                .name("testuser")
                .password("encodedPassword")
                .roles(userRoles)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("testuser")
                .password("encodedPassword")
                .roles(userRoles)
                .build();

        when(mapper.toEntity(eq(validUserRequest), eq(encoder), eq(roleRepository)))
                .thenReturn(mappedUser);
        when(userRepository.save(mappedUser))
                .thenReturn(savedUser);
        when(mapper.toDto(savedUser))
                .thenReturn(expectedUserResponse);


        UserResponse result = userService.createUser(validUserRequest);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("testuser");

        verify(mapper).toEntity(validUserRequest, encoder, roleRepository);
        verify(userRepository).save(mappedUser);
        verify(mapper).toDto(savedUser);
    }

    @Test
    @DisplayName("Когда обновляем существующего пользователя, тогда пользователь обновляется")
    @Transactional
    void updateUser_existingUser_updatesUser() {

        UserRequest updateRequest = new UserRequest(
                "updatedUser",
                "newPassword123",
                List.of("ROLE_USER", "ROLE_ADMIN").toArray(String[]::new)
        );

        List<Role> updatedRoles = new ArrayList<>(userRoles);
        updatedRoles.add(builder().id(2L).name(RoleName.ROLE_ADMIN).build());

        User updatedUser = User.builder()
                .id(1L)
                .name("updatedUser")
                .password("encodedNewPassword")
                .roles(updatedRoles)
                .cards(new ArrayList<>())
                .build();

        UserResponse expectedUpdatedResponse = new UserResponse(
                1L,
                "updatedUser",
                "hash",
                List.of("ROLE_USER", "ROLE_ADMIN")
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(encoder.encode("newPassword123"))
                .thenReturn("encodedNewPassword");
        when(mapper.mapRoles(List.of("ROLE_USER", "ROLE_ADMIN").toArray(String[]::new), roleRepository))
                .thenReturn(updatedRoles);
        when(userRepository.save(any(User.class)))
                .thenReturn(updatedUser);
        when(mapper.toDto(updatedUser))
                .thenReturn(expectedUpdatedResponse);


        UserResponse result = userService.updateUser(1L, updateRequest);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("updatedUser");
        assertThat(result.roles()).contains("ROLE_USER", "ROLE_ADMIN");

        verify(userRepository).findById(1L);
        verify(encoder).encode("newPassword123");
        verify(mapper).mapRoles(List.of("ROLE_USER", "ROLE_ADMIN").toArray(String[]::new), roleRepository);
        verify(userRepository).save(any(User.class));
        verify(mapper).toDto(updatedUser);
    }

    @Test
    @DisplayName("Когда обновляем несуществующего пользователя, тогда создается новый пользователь")
    @Transactional
    void updateUser_nonExistentUser_createsNewUser() {

        UserRequest newUserRequest = new UserRequest(
                "newuser",
                "password123",
                List.of("ROLE_USER").toArray(String[]::new)
        );

        User newUser = User.builder()
                .id(null)
                .name("newuser")
                .password("encodedPassword")
                .roles(userRoles)
                .build();

        User savedUser = User.builder()
                .id(2L)
                .name("newuser")
                .password("encodedPassword")
                .roles(userRoles)
                .build();

        UserResponse expectedNewResponse = new UserResponse(
                2L,
                "newuser",
                "hash",
                List.of("ROLE_USER")
        );

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());
        when(mapper.toEntity(eq(newUserRequest), eq(encoder), eq(roleRepository)))
                .thenReturn(newUser);
        when(userRepository.save(newUser))
                .thenReturn(savedUser);
        when(mapper.toDto(savedUser))
                .thenReturn(expectedNewResponse);


        UserResponse result = userService.updateUser(999L, newUserRequest);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("newuser");

        verify(userRepository).findById(999L);
        verify(mapper).toEntity(newUserRequest, encoder, roleRepository);
        verify(userRepository).save(newUser);
        verify(mapper).toDto(savedUser);
    }


}