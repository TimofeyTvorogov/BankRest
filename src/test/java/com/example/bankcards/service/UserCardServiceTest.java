package com.example.bankcards.service;

import com.example.bankcards.dto.userFuncs.CardFilter;
import com.example.bankcards.dto.userFuncs.CardSearchRequest;
import com.example.bankcards.dto.userFuncs.CardDataUserResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundExcepion;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.mappers.CardMapper;

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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.Card.*;
import static com.example.bankcards.entity.Card.CardStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
class UserCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private UserCardService userCardService;

    private User testUser;
    private Card activeCard;
    private Card blockedCard;
    private Card expiredCard;
    private CardDataUserResponse cardUserResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("testuser")
                .cards(new ArrayList<>())
                .build();

        activeCard = builder()
                .id(100L)
                .cardNum("1234567890123456")
                .owner(testUser)
                .balance(BigDecimal.valueOf(1000))
                .status(ACTIVE)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        blockedCard = builder()
                .id(200L)
                .cardNum("9876543210987654")
                .owner(testUser)
                .balance(BigDecimal.valueOf(500))
                .status(BLOCKED)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        expiredCard = builder()
                .id(300L)
                .cardNum("1111222233334444")
                .owner(testUser)
                .balance(BigDecimal.valueOf(200))
                .status(EXPIRED)
                .activeTill(LocalDate.now().minusDays(1))
                .build();

        cardUserResponse = new CardDataUserResponse(
                "************3456",
                LocalDate.now().plusDays(30),
                ACTIVE,
                BigDecimal.valueOf(1000)
        );
    }

    @Test
    @DisplayName("Когда получаем баланс существующей карты, тогда возвращается CardDataUserResponse")
    void getBalance_existingCard_returnsCardDataUserResponse() {

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(activeCard));
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);


        CardDataUserResponse result = userCardService.getBalance(100L);


        assertThat(result).isNotNull();
        assertThat(result.balance()).isEqualTo(BigDecimal.valueOf(1000));

        verify(cardRepository).findById(100L);
        verify(cardMapper).toUserDto(activeCard);
    }

    @Test
    @DisplayName("Когда получаем баланс несуществующей карты, тогда выбрасывается CardNotFoundException")
    void getBalance_nonExistentCard_throwsException() {

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> userCardService.getBalance(999L))
                .isInstanceOf(CardNotFoundExcepion.class)
                .hasMessageContaining("карты с таким id не существует");

        verify(cardRepository).findById(999L);
        verifyNoInteractions(cardMapper);
    }

    @Test
    @DisplayName("Когда запрашиваем блокировку активной карты, тогда статус меняется на BLOCKED")
    void requestBlock_activeCard_changesStatusToBlocked() {

        when(cardRepository.findById(100L))
                .thenReturn(Optional.of(activeCard));


        userCardService.requestBlock(100L);


        verify(cardRepository).findById(100L);
        verify(cardRepository).updateStatusById(100L, BLOCKED);
    }

    @Test
    @DisplayName("Когда запрашиваем блокировку неактивной карты, тогда выбрасывается CardNotActiveException")
    void requestBlock_nonActiveCard_throwsException() {

        when(cardRepository.findById(200L))
                .thenReturn(Optional.of(blockedCard));


        assertThatThrownBy(() -> userCardService.requestBlock(200L))
                .isInstanceOf(CardNotActiveException.class);

        verify(cardRepository).findById(200L);
        verify(cardRepository, never()).updateStatusById(eq(200L), any(CardStatus.class));
    }

    @Test
    @DisplayName("Когда запрашиваем блокировку несуществующей карты, тогда выбрасывается CardNotFoundException")
    void requestBlock_nonExistentCard_throwsException() {

        when(cardRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCardService.requestBlock(999L))
                .isInstanceOf(CardNotFoundExcepion.class);

        verify(cardRepository).findById(999L);
        verify(cardRepository, never()).updateStatusById(any(Long.class), any(CardStatus.class));
    }

    @Test
    @DisplayName("Когда получаем карты пользователя с фильтром и пагинацией, тогда возвращается страница")
    void getCards_withUserIdAndFilter_returnsPage() {

        Long userId = 1L;
        var filter = CardFilter.builder()
                .status(ACTIVE)
                .expiryFrom(LocalDate.now())
                .expiryTo(LocalDate.now().plusYears(1))
                .balanceFrom(BigDecimal.valueOf(100))
                .balanceTo(BigDecimal.valueOf(10000))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard), pageable, 1);

        // Здесь мы не мокаем CardSpecifications, а просто проверяем вызов репозитория
        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);


        Page<CardDataUserResponse> result = userCardService.getCards(userId, filter, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper).toUserDto(activeCard);
    }

    @Test
    @DisplayName("Когда получаем карты пользователя с null фильтром, тогда возвращается страница без фильтрации")
    void getCards_withNullFilter_returnsPage() {

        Long userId = 1L;
        var cardFilter = CardFilter.builder().build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard, blockedCard), pageable, 2);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);
        when(cardMapper.toUserDto(blockedCard))
                .thenReturn(new CardDataUserResponse(
                        "************7654",
                        LocalDate.now().plusDays(30),
                        BLOCKED,
                        BigDecimal.valueOf(500)
                ));


        Page<CardDataUserResponse> result = userCardService.getCards(userId, cardFilter, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper).toUserDto(activeCard);
        verify(cardMapper).toUserDto(blockedCard);
    }

    @Test
    @DisplayName("Когда получаем карты пользователя с пустым фильтром, тогда возвращается страница")
    void getCards_withEmptyFilter_returnsPage() {

        Long userId = 1L;
        CardFilter filter = CardFilter.builder().build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);


        Page<CardDataUserResponse> result = userCardService.getCards(userId, filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Когда ищем карты по номеру с фильтром, тогда возвращается отфильтрованная страница")
    void searchCardsWithNumber_withSearchRequest_returnsFilteredPage() {

        Long userId = 1L;
        CardSearchRequest searchRequest = CardSearchRequest.builder()
                .searchNumber("3456")
                .status(ACTIVE)
                .expiryFrom(LocalDate.now())
                .expiryTo(LocalDate.now().plusYears(1))
                .balanceFrom(BigDecimal.ZERO)
                .balanceTo(BigDecimal.valueOf(10000))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);


        Page<CardDataUserResponse> result = userCardService.searchCardsWithNumber(userId, searchRequest, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper).toUserDto(activeCard);
    }

    @Test
    @DisplayName("Когда ищем карты по номеру без дополнительных фильтров, тогда возвращается страница")
    void searchCardsWithNumber_withOnlyNumber_returnsPage() {

        Long userId = 1L;
        CardSearchRequest searchRequest = CardSearchRequest.builder()
                .searchNumber("3456")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);

        Page<CardDataUserResponse> result = userCardService.searchCardsWithNumber(userId, searchRequest, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Когда ищем карты по номеру с null searchNumber, тогда возвращается страница")
    void searchCardsWithNumber_withNullNumber_returnsPage() {

        Long userId = 1L;
        CardSearchRequest searchRequest = CardSearchRequest.builder()
                .searchNumber(null)
                .status(ACTIVE)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);


        Page<CardDataUserResponse> result = userCardService.searchCardsWithNumber(userId, searchRequest, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Когда ищем карты с пустым поисковым номером, тогда возвращается страница")
    void searchCardsWithNumber_withEmptyNumber_returnsPage() {

        Long userId = 1L;
        CardSearchRequest searchRequest = CardSearchRequest.builder()
                .searchNumber("")
                .status(ACTIVE)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(activeCard), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(activeCard))
                .thenReturn(cardUserResponse);


        Page<CardDataUserResponse> result = userCardService.searchCardsWithNumber(userId, searchRequest, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Когда запрашиваем блокировку просроченной карты, тогда выбрасывается CardNotActiveException")
    void requestBlock_expiredCard_throwsException() {

        when(cardRepository.findById(300L))
                .thenReturn(Optional.of(expiredCard));


        assertThatThrownBy(() -> userCardService.requestBlock(300L))
                .isInstanceOf(CardNotActiveException.class);

        verify(cardRepository).findById(300L);
        verify(cardRepository, never()).updateStatusById(eq(300L), any(CardStatus.class));
    }

    @Test
    @DisplayName("Когда получаем карты с разными статусами, тогда спецификация строится корректно")
    void getCards_withDifferentStatuses_callsRepositoryWithSpecification() {

        Long userId = 1L;
        CardFilter filter = CardFilter.builder()
                .status(BLOCKED)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(blockedCard), pageable, 1);

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(cardPage);
        when(cardMapper.toUserDto(blockedCard))
                .thenReturn(new CardDataUserResponse(
                        "************7654",
                        LocalDate.now().plusDays(30),
                        BLOCKED,
                        BigDecimal.valueOf(500)
                ));


        Page<CardDataUserResponse> result = userCardService.getCards(userId, filter, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);


        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }
}