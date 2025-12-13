package com.example.bankcards.service;

import com.example.bankcards.dto.adminFuncs.CardDataAdminRequest;
import com.example.bankcards.dto.adminFuncs.CardDataAdminResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardHasExpiredException;
import com.example.bankcards.exception.CardNotFoundExcepion;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mappers.CardMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.Card.CardStatus.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AdminCardServiceTest {


    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;


    @InjectMocks
    private AdminCardService adminCardService;


    private User testUser;
    private Card activeCard;
    private Card blockedCard;
    private Card expiredCard;
    private CardDataAdminRequest validRequest;

    @BeforeEach
    void setUp() {

        testUser = User.builder()
                .id(1L)
                .name("testuser")
                .cards(new ArrayList<>())
                .build();

        activeCard = Card.builder()
                .id(100L)
                .cardNum("1234567890123456")
                .owner(testUser)
                .balance(BigDecimal.valueOf(1000))
                .status(ACTIVE)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        blockedCard = Card.builder()
                .id(200L)
                .status(BLOCKED)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        expiredCard = Card.builder()
                .id(300L)
                .status(EXPIRED)
                .activeTill(LocalDate.now().minusDays(1))
                .build();

        validRequest = new CardDataAdminRequest(
                "1111222233334444",
                LocalDate.now().plusYears(2),
                1l,
                BigDecimal.valueOf(5000)
        );

    }

    @Test
    @DisplayName("Когда добавляем карту с валидными данными, тогда карта сохраняется")
    void addCard_withValidData_savesCard() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        Card newCard = Card.builder()
                .id(999L)
                .cardNum("1111222233334444")
                .build();

        Card savedCard = Card.builder()
                .id(999L)
                .cardNum("1111222233334444")
                .owner(testUser)
                .build();

        CardDataAdminResponse expectedResponse = new CardDataAdminResponse(
                999L, "************4444", LocalDate.now().plusDays(30), "id = 1, admin", ACTIVE);


        when(cardMapper.toEntity(any(CardDataAdminRequest.class), eq(userRepository)))
                .thenReturn(newCard);

        when(cardRepository.saveAndFlush(newCard))
                .thenReturn(savedCard);

        when(cardMapper.toAdminDto(savedCard))
                .thenReturn(expectedResponse);


        CardDataAdminResponse result = adminCardService.addCard(validRequest);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(999L);
        assertThat(result.maskedCardNum()).isEqualTo("************4444");


        verify(userRepository).findById(1L);
        verify(cardMapper).toEntity(validRequest, userRepository);
        verify(cardRepository).saveAndFlush(newCard);
    }
    @Test
    @DisplayName("Когда добавляем карту несуществующему пользователю, тогда выбрасывается UserNotFoundException")
    void addCard_withNonExistentUser_throwsException() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        CardDataAdminRequest request = new CardDataAdminRequest(
                "1111222233334444",
                LocalDate.now().plusYears(2),
                999L, BigDecimal.valueOf(1000.00)
        );


        assertThatThrownBy(() -> adminCardService.addCard(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findById(999L);
        verifyNoInteractions(cardRepository, cardMapper);
    }
    @Test
    @DisplayName("Когда блокируем активную карту, тогда статус меняется на BLOCKED")
    void blockCard_activeCard_changesStatusToBlocked() {

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(activeCard));


        adminCardService.blockCard(100L);


        verify(cardRepository).updateStatusById(100L, BLOCKED);
    }

    @Test
    @DisplayName("Когда блокируем просроченную карту, тогда выбрасывается CardHasExpiredException")
    void blockCard_expiredCard_throwsException() {

        when(cardRepository.findById(300L))
                .thenReturn(Optional.of(expiredCard));


        assertThatThrownBy(() -> adminCardService.blockCard(300L))
                .isInstanceOf(CardHasExpiredException.class);

        verify(cardRepository, never()).updateStatusById(any(), any());
    }

    @Test
    @DisplayName("Когда блокируем несуществующую карту, тогда выбрасывается CardNotFoundException")
    void blockCard_nonExistentCard_throwsException() {

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCardService.blockCard(999L))
                .isInstanceOf(CardNotFoundExcepion.class)
                .hasMessageContaining("999");
    }
    @Test
    @DisplayName("Когда активируем заблокированную карту с валидным сроком, тогда статус меняется на ACTIVE")
    void activateCard_blockedCardWithValidDate_activates() {

        when(cardRepository.findById(200L))
                .thenReturn(Optional.of(blockedCard));


        adminCardService.activateCard(200L);


        verify(cardRepository).updateStatusById(200L, ACTIVE);
    }

    @Test
    @DisplayName("Когда активируем уже активную карту, тогда ничего не происходит")
    void activateCard_alreadyActiveCard_doesNothing() {

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(activeCard));

        adminCardService.activateCard(100L);

        verify(cardRepository, never()).updateStatusById(any(), any());
    }

    @Test
    @DisplayName("Когда активируем просроченную карту, тогда выбрасывается CardHasExpiredException")
    void activateCard_expiredCard_throwsException() {

        when(cardRepository.findById(300L))
                .thenReturn(Optional.of(expiredCard));


        assertThatThrownBy(() -> adminCardService.activateCard(300L))
                .isInstanceOf(CardHasExpiredException.class);

        verify(cardRepository, never()).updateStatusById(any(), any());
    }
    @Test
    @DisplayName("Когда удаляем существующую карту, тогда она удаляется")
    void deleteCard_existingCard_deletesCard() {

        when(cardRepository.existsById(100L))
                .thenReturn(true);


        adminCardService.deleteCard(100L);


        verify(cardRepository).deleteById(100L);
    }

    @Test
    @DisplayName("Когда удаляем несуществующую карту, тогда выбрасывается CardNotFoundException")
    void deleteCard_nonExistentCard_throwsException() {

        when(cardRepository.existsById(999L))
                .thenReturn(false);


        assertThatThrownBy(() -> adminCardService.deleteCard(999L))
                .isInstanceOf(CardNotFoundExcepion.class);

        verify(cardRepository, never()).deleteById(any());
    }
    @Test
    @DisplayName("Когда получаем карты с пагинацией, тогда возвращается страница")
    void getCards_withPageable_returnsPage() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard, blockedCard), pageable, 2);

        CardDataAdminResponse response1 = new CardDataAdminResponse(100L, "************1234", LocalDate.now().plusDays(30), "id = 1, admin", ACTIVE);
        CardDataAdminResponse response2 = new CardDataAdminResponse(200L, "************5678", LocalDate.now().plusDays(30), "null", BLOCKED);

        when(cardRepository.findAll(pageable))
                .thenReturn(cardPage);

        when(cardMapper.toAdminDto(activeCard))
                .thenReturn(response1);

        when(cardMapper.toAdminDto(blockedCard))
                .thenReturn(response2);


        Page<CardDataAdminResponse> result = adminCardService.getCards(pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
        assertThat(result.getContent().get(1).id()).isEqualTo(200L);
    }
    @Test
    @DisplayName("Когда получаем карту по существующему ID, тогда возвращается карта")
    void getCardById_existingId_returnsCard() {

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(activeCard));

        CardDataAdminResponse expectedResponse = new CardDataAdminResponse(
                100L,
                "************3456",
                LocalDate.now().plusDays(30),
                "id = 1, admin", ACTIVE
        );

        when(cardMapper.toAdminDto(activeCard))
                .thenReturn(expectedResponse);


        CardDataAdminResponse result = adminCardService.getCardById(100L);


        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Когда получаем карту по несуществующему ID, тогда выбрасывается CardNotFoundException")
    void getCardById_nonExistentId_throwsException() {

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> adminCardService.getCardById(999L))
                .isInstanceOf(CardNotFoundExcepion.class);
    }
}