package com.example.bankcards.service;

import com.example.bankcards.dto.userFuncs.transfer.TransferRequest;
import com.example.bankcards.dto.userFuncs.transfer.TransferResponse;
import com.example.bankcards.dto.userFuncs.transfer.TransferSummary;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.CardNotFoundExcepion;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mappers.CardMapper;
import com.example.bankcards.util.mappers.TransferMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.Card.CardStatus.ACTIVE;
import static com.example.bankcards.entity.Card.CardStatus.BLOCKED;
import static com.example.bankcards.entity.Transfer.TransferStatus.COMPLETED;
import static com.example.bankcards.entity.Transfer.TransferStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
class TransferServiceTest {

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest validTransferRequest;
    private Transfer savedTransfer;
    private TransferResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("testuser")
                .cards(new ArrayList<>())
                .build();

        fromCard = Card.builder()
                .id(100L)
                .cardNum("1234567890123456")
                .owner(testUser)
                .balance(BigDecimal.valueOf(5000))
                .status(ACTIVE)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        toCard = Card.builder()
                .id(200L)
                .cardNum("9876543210987654")
                .owner(testUser)
                .balance(BigDecimal.valueOf(1000))
                .status(ACTIVE)
                .activeTill(LocalDate.now().plusDays(60))
                .build();

        validTransferRequest = new TransferRequest(
                100L,
                200L,
                BigDecimal.valueOf(1000),
                "Test transfer"
        );

        savedTransfer = Transfer.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(BigDecimal.valueOf(1000))
                .description("Test transfer")
                .createdAt(LocalDateTime.now())
                .initiatedBy(testUser)
                .build();

        expectedResponse = new TransferResponse(
                1L,
                "************3456",
                "************7654",
                BigDecimal.valueOf(1000),
                COMPLETED.toString(),
                "Test transfer",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Когда создаем перевод с валидными данными, тогда перевод успешно создается")
    @org.springframework.transaction.annotation.Transactional
    void createTransfer_withValidData_createsTransfer() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.of(toCard));
        when(cardMapper.mapCardNumFromEntity(fromCard))
                .thenReturn("************3456");
        when(transferMapper.toEntity(eq(validTransferRequest), eq(cardRepository), eq(testUser)))
                .thenReturn(savedTransfer);
        when(transferRepository.save(savedTransfer))
                .thenReturn(savedTransfer);
        when(transferMapper.toResponse(savedTransfer))
                .thenReturn(expectedResponse);


        TransferResponse result = transferService.createTransfer(validTransferRequest, 1L);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.amount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.description()).isEqualTo("Test transfer");


        assertThat(fromCard.getBalance()).isEqualTo(BigDecimal.valueOf(4000)); // 5000 - 1000
        assertThat(toCard.getBalance()).isEqualTo(BigDecimal.valueOf(2000));   // 1000 + 1000

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verify(cardRepository).saveAll(List.of(fromCard, toCard));
        verify(transferMapper).toEntity(validTransferRequest, cardRepository, testUser);
        verify(transferRepository).save(savedTransfer);
        verify(transferMapper).toResponse(savedTransfer);
    }

    @Test
    @DisplayName("Когда создаем перевод с несуществующим пользователем, тогда выбрасывается UserNotFoundException")
    void createTransfer_withNonExistentUser_throwsException() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findById(999L);
        verifyNoInteractions(cardRepository, transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с несуществующей картой отправителя, тогда выбрасывается CardNotFoundException")
    void createTransfer_withNonExistentFromCard_throwsException() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 1L))
                .isInstanceOf(CardNotFoundExcepion.class)
                .hasMessageContaining("100");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository, never()).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с несуществующей картой получателя, тогда выбрасывается CardNotFoundException")
    void createTransfer_withNonExistentToCard_throwsException() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.empty());
        when(cardMapper.mapCardNumFromEntity(fromCard))
                .thenReturn("************3456");


        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 1L))
                .isInstanceOf(CardNotFoundExcepion.class)
                .hasMessageContaining("200");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с неактивной картой отправителя, тогда выбрасывается CardNotActiveException")
    void createTransfer_withInactiveFromCard_throwsException() {

        Card blockedCard = Card.builder()
                .id(100L)
                .cardNum("1234567890123456")
                .owner(testUser)
                .balance(BigDecimal.valueOf(5000))
                .status(BLOCKED)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(blockedCard));
        when(cardMapper.mapCardNumFromEntity(blockedCard))
                .thenReturn("************3456");


        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 1L))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("3456");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository, never()).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с неактивной картой получателя, тогда выбрасывается CardNotActiveException")
    void createTransfer_withInactiveToCard_throwsException() {

        Card blockedCard = Card.builder()
                .id(200L)
                .cardNum("9876543210987654")
                .owner(testUser)
                .balance(BigDecimal.valueOf(1000))
                .status(BLOCKED)
                .activeTill(LocalDate.now().plusDays(30))
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.of(blockedCard));
        when(cardMapper.mapCardNumFromEntity(fromCard))
                .thenReturn("************3456");
        when(cardMapper.mapCardNumFromEntity(blockedCard))
                .thenReturn("************7654");


        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 1L))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("7654");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод на ту же карту, тогда выбрасывается IllegalStateException")
    void createTransfer_sameCard_throwsException() {

        TransferRequest sameCardRequest = new TransferRequest(
                100L,
                100L,
                BigDecimal.valueOf(1000),
                "Same card transfer"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardMapper.mapCardNumFromEntity(fromCard))
                .thenReturn("************3456");


        assertThatThrownBy(() -> transferService.createTransfer(sameCardRequest, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("перевод должен осуществляться на разные карты");

        verify(userRepository).findById(1L);
        verify(cardRepository, times(2)).findByIdAndOwnerId(100L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с недостаточным балансом, тогда выбрасывается InsufficientFundsException")
    void createTransfer_withInsufficientFunds_throwsException() {

        TransferRequest largeAmountRequest = new TransferRequest(
                100L,
                200L,
                BigDecimal.valueOf(10000), // Больше чем баланс 5000
                "Large transfer"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.of(toCard));
        when(cardMapper.mapCardNumFromEntity(fromCard))
                .thenReturn("************3456");



        assertThatThrownBy(() -> transferService.createTransfer(largeAmountRequest, 1L))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("3456");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с нулевой суммой, тогда выбрасывается исключение")
    @org.springframework.transaction.annotation.Transactional
    void createTransfer_withZeroAmount_throwsException() {

        TransferRequest zeroAmountRequest = new TransferRequest(
                100L,
                200L,
                BigDecimal.ZERO,
                "Zero transfer"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.of(toCard));


        assertThatThrownBy(() -> transferService.createTransfer(zeroAmountRequest, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("сумма перевода должна быть положительным числом");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verify(cardRepository, never()).saveAll(any()); // Не должен вызываться
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод со всей суммой с карты, тогда перевод успешно создается")
    void createTransfer_withExactBalanceAmount_createsTransfer() {

        TransferRequest request = new TransferRequest(
                100L,
                200L,
                BigDecimal.valueOf(5000),
                "Весь баланс"
        );


        Transfer savedTransfer = Transfer.builder()
                .id(999L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(BigDecimal.valueOf(5000))
                .description("Весь баланс")
                .createdAt(LocalDateTime.now())
                .initiatedBy(testUser)
                .build();

        TransferResponse expectedResponse = new TransferResponse(
                999L,
                "************3456",
                "************7654",
                BigDecimal.valueOf(5000),
                COMPLETED.toString(),
                "Перевод всех средств",
                LocalDateTime.now(),
                LocalDateTime.now()
        );


        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));

        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.of(toCard));

        when(cardMapper.mapCardNumFromEntity(any(Card.class)))
                .thenReturn("************0000");


        when(transferMapper.toEntity(any(TransferRequest.class), any(), any()))
                .thenReturn(Transfer.builder().build());


        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);


        when(transferMapper.toResponse(any(Transfer.class)))
                .thenReturn(expectedResponse);


        TransferResponse result = transferService.createTransfer(request, 1L);


        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(999L);
        assertThat(result.amount()).isEqualTo(BigDecimal.valueOf(5000));


        assertThat(fromCard.getBalance())
                .isEqualTo(BigDecimal.ZERO)
                .withFailMessage("Баланс карты отправителя должен стать 0");


        assertThat(toCard.getBalance())
                .isEqualTo(BigDecimal.valueOf(6000))
                .withFailMessage("Баланс карты получателя должен стать 6000");


        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verify(cardRepository).saveAll(List.of(fromCard, toCard));
        verify(transferMapper).toEntity(any(TransferRequest.class), any(), any());
        verify(transferRepository).save(any(Transfer.class));
        verify(transferMapper).toResponse(any(Transfer.class));
    }

    @Test
    @DisplayName("Когда получаем историю переводов с пагинацией, тогда возвращается страница")
    void getTransferHistory_withPageable_returnsPage() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfer> transferPage = new PageImpl<>(List.of(savedTransfer), pageable, 1);

        TransferSummary summary = new TransferSummary(
                "************3456",
                "************7654",
                BigDecimal.valueOf(1000),
                PENDING.toString(),
                LocalDateTime.now(),
                "какой-то перевод"
        );

        when(transferRepository.findAllByInitiatedById(pageable, 1L))
                .thenReturn(transferPage);
        when(transferMapper.toSummary(savedTransfer))
                .thenReturn(summary);


        Page<TransferSummary> result = transferService.getTransferHistory(pageable, 1L);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).amount()).isEqualTo(BigDecimal.valueOf(1000));

        verify(transferRepository).findAllByInitiatedById(pageable, 1L);
        verify(transferMapper).toSummary(savedTransfer);
    }

    @Test
    @DisplayName("Когда получаем историю переводов для пользователя без переводов, тогда возвращается пустая страница")
    void getTransferHistory_userWithNoTransfers_returnsEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Transfer> emptyPage = Page.empty(pageable);

        when(transferRepository.findAllByInitiatedById(pageable, 1L))
                .thenReturn(emptyPage);


        Page<TransferSummary> result = transferService.getTransferHistory(pageable, 1L);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(transferRepository).findAllByInitiatedById(pageable, 1L);
        verifyNoInteractions(transferMapper);
    }

    @Test
    @DisplayName("Когда карта отправителя не принадлежит пользователю, тогда выбрасывается CardNotFoundException")
    void createTransfer_fromCardNotOwnedByUser_throwsException() {

        User anotherUser = User.builder()
                .id(2L)
                .name("anotheruser")
                .build();



        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 1L))
                .isInstanceOf(CardNotFoundExcepion.class)
                .hasMessageContaining("100");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository, never()).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда карта получателя не принадлежит пользователю, тогда выбрасывается CardNotFoundException")
    void createTransfer_toCardNotOwnedByUser_throwsException() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.empty());
        when(cardMapper.mapCardNumFromEntity(fromCard))
                .thenReturn("************3456");

        assertThatThrownBy(() -> transferService.createTransfer(validTransferRequest, 1L))
                .isInstanceOf(CardNotFoundExcepion.class)
                .hasMessageContaining("200");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }

    @Test
    @DisplayName("Когда создаем перевод с отрицательной суммой, тогда выбрасывается IllegalArgumentException")
    void createTransfer_withNegativeAmount_throwsException() {

        TransferRequest negativeAmountRequest = new TransferRequest(
                100L,
                200L,
                BigDecimal.valueOf(-100),
                "Negative transfer"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findByIdAndOwnerId(100L, 1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndOwnerId(200L, 1L))
                .thenReturn(Optional.of(toCard));


        assertThatThrownBy(() -> transferService.createTransfer(negativeAmountRequest, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("сумма перевода должна быть положительным числом");

        verify(userRepository).findById(1L);
        verify(cardRepository).findByIdAndOwnerId(100L, 1L);
        verify(cardRepository).findByIdAndOwnerId(200L, 1L);
        verifyNoInteractions(transferMapper, transferRepository);
    }
}