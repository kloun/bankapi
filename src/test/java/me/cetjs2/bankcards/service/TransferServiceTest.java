package me.cetjs2.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import me.cetjs2.bankcards.dto.SucessActionResponse;
import me.cetjs2.bankcards.dto.TransferRequest;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.entity.User;
import me.cetjs2.bankcards.exception.CardNotFoundException;
import me.cetjs2.bankcards.exception.CardPermissionDeniedException;
import me.cetjs2.bankcards.exception.InsufficientFundsException;
import me.cetjs2.bankcards.repository.CardRepository;
import me.cetjs2.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

  @Mock private CardRepository cardRepository;
  @Mock private CardUtil cardUtil;
  @Mock private UserService userService;

  @InjectMocks private TransferService transferService;

  private User testUser;
  private Card cardFrom;
  private Card cardTo;
  private TransferRequest validRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder().userName("ivan_gold").build();
    UUID fromId = UUID.randomUUID();
    UUID toId = UUID.randomUUID();

    cardFrom =
        Card.builder()
            .id(fromId)
            .cardNumber("1111222233334444")
            .balance(new BigDecimal("1000.00"))
            .cardStatus(CardStatus.ACTIVE)
            .owner(testUser)
            .build();

    cardTo =
        Card.builder()
            .id(toId)
            .cardNumber("5555666677778888")
            .balance(new BigDecimal("100.00"))
            .cardStatus(CardStatus.ACTIVE)
            .owner(testUser)
            .build();

    validRequest = new TransferRequest(fromId, toId, new BigDecimal("500.00"));
  }

  @Test
  @DisplayName("Успешный перевод между своими картами")
  void transferMoney_Success() {
    // GIVEN
    when(cardRepository.findById(cardFrom.getId())).thenReturn(Optional.of(cardFrom));
    when(cardRepository.findById(cardTo.getId())).thenReturn(Optional.of(cardTo));
    when(userService.getCurrentUser()).thenReturn(testUser);

    // Проверка владения
    when(cardRepository.existsByIdAndOwnerUserName(cardFrom.getId(), testUser.getUsername()))
        .thenReturn(true);
    when(cardRepository.existsByIdAndOwnerUserName(cardTo.getId(), testUser.getUsername()))
        .thenReturn(true);

    when(cardUtil.maskCardNumber(anyString())).thenReturn("XXXX-XXXX");

    // WHEN
    SucessActionResponse response = transferService.transferMoney(validRequest);

    // THEN
    assertThat(response.message()).contains("500.00");

    // Проверяем списание (1000 - 500 = 500)
    verify(cardRepository)
        .save(
            argThat(
                c ->
                    c.getId().equals(cardFrom.getId())
                        && c.getBalance().compareTo(new BigDecimal("500.00")) == 0));

    // Проверяем зачисление (100 + 500 = 600)
    verify(cardRepository)
        .save(
            argThat(
                c ->
                    c.getId().equals(cardTo.getId())
                        && c.getBalance().compareTo(new BigDecimal("600.00")) == 0));
  }

  @Test
  @DisplayName("Ошибка: карта не найдена")
  void transferMoney_CardNotFound() {
    when(cardRepository.findById(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transferService.transferMoney(validRequest))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @DisplayName("Ошибка: попытка перевода с чужой карты")
  void transferMoney_PermissionDenied_NotOwner() {
    when(cardRepository.findById(cardFrom.getId())).thenReturn(Optional.of(cardFrom));
    when(cardRepository.findById(cardTo.getId())).thenReturn(Optional.of(cardTo));
    when(userService.getCurrentUser()).thenReturn(testUser);

    // Имитируем, что карта отправителя не принадлежит юзеру
    when(cardRepository.existsByIdAndOwnerUserName(cardFrom.getId(), testUser.getUsername()))
        .thenReturn(false);

    assertThatThrownBy(() -> transferService.transferMoney(validRequest))
        .isInstanceOf(CardPermissionDeniedException.class)
        .hasMessageContaining("принадлежат другому пользователю");
  }

  @Test
  @DisplayName("Ошибка: одна из карт заблокирована")
  void transferMoney_PermissionDenied_CardBlocked() {
    cardFrom = cardFrom.toBuilder().cardStatus(CardStatus.BLOCKED).build();

    when(cardRepository.findById(cardFrom.getId())).thenReturn(Optional.of(cardFrom));
    when(cardRepository.findById(cardTo.getId())).thenReturn(Optional.of(cardTo));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(cardRepository.existsByIdAndOwnerUserName(any(), any())).thenReturn(true);

    assertThatThrownBy(() -> transferService.transferMoney(validRequest))
        .isInstanceOf(CardPermissionDeniedException.class)
        .hasMessageContaining("заблокированы");
  }

  @Test
  @DisplayName("Ошибка: недостаточно средств")
  void transferMoney_InsufficientFunds() {
    TransferRequest expensiveRequest =
        new TransferRequest(cardFrom.getId(), cardTo.getId(), new BigDecimal("5000.00"));

    when(cardRepository.findById(cardFrom.getId())).thenReturn(Optional.of(cardFrom));
    when(cardRepository.findById(cardTo.getId())).thenReturn(Optional.of(cardTo));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(cardRepository.existsByIdAndOwnerUserName(any(), any())).thenReturn(true);

    assertThatThrownBy(() -> transferService.transferMoney(expensiveRequest))
        .isInstanceOf(InsufficientFundsException.class);
  }
}
