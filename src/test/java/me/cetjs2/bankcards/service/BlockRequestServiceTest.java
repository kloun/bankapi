package me.cetjs2.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import me.cetjs2.bankcards.dto.SucessActionResponse;
import me.cetjs2.bankcards.entity.BlockRequest;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.entity.Role;
import me.cetjs2.bankcards.entity.User;
import me.cetjs2.bankcards.exception.CardNotFoundException;
import me.cetjs2.bankcards.exception.CardPermissionDeniedException;
import me.cetjs2.bankcards.repository.BlockRequestRepository;
import me.cetjs2.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BlockRequestServiceTest {

  @Mock private BlockRequestRepository blockRequestRepository;
  @Mock private CardRepository cardRepository;
  @Mock private UserService userService;

  @InjectMocks private BlockRequestService blockRequestService;

  private User testUser;
  private Card testCard;
  private UUID cardId;

  @BeforeEach
  void setUp() {
    cardId = UUID.randomUUID();
    testUser = User.builder().userName("owner_user").role(Role.USER).build();
    testCard = Card.builder().id(cardId).owner(testUser).cardStatus(CardStatus.ACTIVE).build();
  }

  @Test
  @DisplayName("Успешное создание запроса на блокировку")
  void sendBlockRequest_Success() {
    // GIVEN
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(blockRequestRepository.existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(cardId))
        .thenReturn(false);

    // WHEN
    SucessActionResponse response = blockRequestService.sendBlockRequest(cardId);

    // THEN
    assertThat(response.message()).isEqualTo("Запрос на блокировку карты отправлен");
    verify(blockRequestRepository)
        .save(
            argThat(
                request -> request.getCard().equals(testCard) && request.getCreatedDate() != null));
  }

  @Test
  @DisplayName("Ошибка: Карта не найдена")
  void sendBlockRequest_CardNotFound() {
    when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> blockRequestService.sendBlockRequest(cardId))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @DisplayName("Ошибка: Попытка заблокировать чужую карту")
  void sendBlockRequest_PermissionDenied_WrongOwner() {
    User stranger = User.builder().userName("stranger").build();

    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(userService.getCurrentUser()).thenReturn(stranger); // Текущий юзер не владелец

    assertThatThrownBy(() -> blockRequestService.sendBlockRequest(cardId))
        .isInstanceOf(CardPermissionDeniedException.class);

    verify(blockRequestRepository, never()).save(any());
  }

  @Test
  @DisplayName("Инфо: Запрос уже существует в БД")
  void sendBlockRequest_AlreadyExists() {
    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(blockRequestRepository.existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(cardId))
        .thenReturn(true);

    SucessActionResponse response = blockRequestService.sendBlockRequest(cardId);

    assertThat(response.message()).contains("ожидайте решения");
    verify(blockRequestRepository, never()).save(any(BlockRequest.class));
  }

  @Test
  @DisplayName("Ошибка: Карта уже имеет статус BLOCKED или EXPIRED")
  void sendBlockRequest_InvalidCardStatus() {
    testCard = testCard.toBuilder().cardStatus(CardStatus.BLOCKED).build();

    when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(blockRequestRepository.existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(cardId))
        .thenReturn(false);

    assertThatThrownBy(() -> blockRequestService.sendBlockRequest(cardId))
        .isInstanceOf(CardPermissionDeniedException.class)
        .hasMessageContaining("уже заблокирована");
  }
}
