package me.cetjs2.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.cetjs2.bankcards.dto.CardViewResponse;
import me.cetjs2.bankcards.dto.CreateCardRequest;
import me.cetjs2.bankcards.dto.SucessActionResponse;
import me.cetjs2.bankcards.entity.BlockRequest;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.entity.User;
import me.cetjs2.bankcards.exception.UsernameNotFoundException;
import me.cetjs2.bankcards.repository.BlockRequestRepository;
import me.cetjs2.bankcards.repository.CardRepository;
import me.cetjs2.bankcards.repository.UserRepository;
import me.cetjs2.bankcards.util.CardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

  @Mock private CardRepository cardRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserService userService;
  @Mock private BlockRequestRepository blockRequestRepository;
  @Mock private CardUtil cardUtil;

  @InjectMocks private CardService cardService;

  private User testUser;
  private UUID cardId;
  private Card testCard;

  @BeforeEach
  void setUp() {
    testUser = User.builder().userName("ivan_petrov").build();
    cardId = UUID.randomUUID();
    testCard =
        Card.builder()
            .id(cardId)
            .cardNumber("4444 4000 0000 1111")
            .owner(testUser)
            .cardStatus(CardStatus.ACTIVE)
            .expirationDate(YearMonth.now().plusYears(2))
            .balance(BigDecimal.TEN)
            .build();
  }

  @Nested
  @DisplayName("Создание карты")
  class CreateCardTests {
    @Test
    @DisplayName("Успешное создание со всеми полями")
    void createCard_Success() {
      var request =
          new CreateCardRequest(
              testUser.getUsername(), BigDecimal.valueOf(500), YearMonth.now().plusYears(1));

      when(userRepository.findByUserName(testUser.getUsername())).thenReturn(Optional.of(testUser));
      when(cardUtil.generateCardNumber(anyString())).thenReturn("4444 4999 9999 9999");
      when(cardUtil.maskCardNumber(anyString())).thenReturn("4444 4*** **** 9999");
      when(cardRepository.save(any(Card.class))).thenReturn(testCard);

      CardViewResponse response = cardService.createCard(request);

      assertThat(response).isNotNull();
      assertThat(response.ownerName()).isEqualTo(testUser.getUsername());
      verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Ошибка, если владелец не найден в БД")
    void createCard_UserNotFound() {
      when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  cardService.createCard(
                      new CreateCardRequest("unknown", BigDecimal.ZERO, YearMonth.now())))
          .isInstanceOf(UsernameNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("Получение и удаление")
  class AccessTests {
    @Test
    @DisplayName("Получение информации по ID")
    void getCardInfo_Success() {
      when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
      when(cardUtil.maskCardNumber(anyString())).thenReturn("masked");

      CardViewResponse response = cardService.getCardInfo(cardId);

      assertThat(response.id()).isEqualTo(cardId);
      verify(cardRepository).findById(cardId);
    }

    @Test
    @DisplayName("Удаление существующей карты")
    void deleteCard_Success() {
      when(cardRepository.existsById(cardId)).thenReturn(true);

      SucessActionResponse response = cardService.deleteCard(cardId);

      assertThat(response.message()).contains("успешно удалена");
      verify(cardRepository).deleteById(cardId);
    }
  }

  @Nested
  @DisplayName("Блокировка карт")
  class BlockTests {
    @Test
    @DisplayName("Блокировка карты и закрытие существующего запроса на блок")
    void blockCard_WithRequestUpdate() {
      BlockRequest pendingRequest = BlockRequest.builder().id(UUID.randomUUID()).build();

      when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
      when(blockRequestRepository.existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(cardId))
          .thenReturn(true);
      when(blockRequestRepository.findOneByCardIdAndApprovedByIsNullAndApprovedDateIsNull(cardId))
          .thenReturn(pendingRequest);
      when(userService.getCurrentUser()).thenReturn(testUser);

      cardService.blockCard(cardId);

      // Проверка смены статуса карты
      verify(cardRepository).save(argThat(c -> c.getCardStatus() == CardStatus.BLOCKED));
      verify(blockRequestRepository)
          .save(
              argThat(
                  req -> req.getApprovedBy().equals(testUser) && req.getApprovedDate() != null));
    }

    @Test
    @DisplayName("Массовая блокировка: корректная обработка карт, просроченных до текущего месяца")
    void blockExpiredCards_ShouldProcessOnlyCardsBeforeCurrentMonth() {
      // 1. GIVEN (Дано)
      // Фиксируем текущий месяц, который будет использовать сервис
      YearMonth today = YearMonth.now();

      // Создаем карту, которая просрочена (например, на 1 месяц назад)
      Card expiredCard1 =
          Card.builder()
              .id(UUID.randomUUID())
              .cardNumber("4444 0000 1111 2222")
              .expirationDate(today.minusMonths(1)) // Например: Декабрь 2025
              .cardStatus(CardStatus.ACTIVE)
              .build();

      // Создаем вторую карту, которая просрочена (например, на год назад)
      Card expiredCard2 =
          Card.builder()
              .id(UUID.randomUUID())
              .cardNumber("4444 0000 3333 4444")
              .expirationDate(today.minusYears(1)) // Например: Январь 2025
              .cardStatus(CardStatus.ACTIVE)
              .build();

      // Настраиваем Mock: репозиторий должен вернуть эти карты при поиске по текущему месяцу
      when(cardRepository.findAllByCardStatusAndExpirationDateBefore(
              eq(CardStatus.ACTIVE), eq(today)))
          .thenReturn(List.of(expiredCard1, expiredCard2));

      // Имитируем сохранение: возвращаем тот же объект, что пришел на вход
      when(cardRepository.save(any(Card.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // 2. WHEN (Когда)
      cardService.blockExpiredCards();

      // 3. THEN (Тогда)
      // Проверяем, что findAll... вызвался ровно 1 раз с правильными параметрами
      verify(cardRepository, times(1))
          .findAllByCardStatusAndExpirationDateBefore(eq(CardStatus.ACTIVE), eq(today));

      // Проверяем, что save() вызвался 2 раза (для каждой карты)
      // и статус каждой карты был изменен на EXPIRED
      verify(cardRepository, times(2))
          .save(argThat(card -> card.getCardStatus() == CardStatus.EXPIRED));

      // Дополнительная проверка: убеждаемся, что ID карт не изменились при сохранении
      verify(cardRepository).save(argThat(c -> c.getId().equals(expiredCard1.getId())));
      verify(cardRepository).save(argThat(c -> c.getId().equals(expiredCard2.getId())));
    }
  }

  @Nested
  @DisplayName(
      "Сценарии пагинации  и показа всех карт в зависимости от того, какая роль у  пользователя")
  class SecurityListCardsPaginationTests {
    private final Pageable pageable = PageRequest.of(0, 5);

    @Test
    @DisplayName("Админ запрашивает все карты (targetUsername пуст)")
    void admin_GetAll() {
      when(cardRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testCard)));

      cardService.getAllAvailableCards("admin", "", true, pageable);

      verify(cardRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Админ фильтрует по конкретному пользователю")
    void admin_GetByTarget() {
      when(cardRepository.findByOwnerUserName("user_a", pageable)).thenReturn(Page.empty());

      cardService.getAllAvailableCards("admin", "user_a", true, pageable);

      verify(cardRepository).findByOwnerUserName("user_a", pageable);
    }

    @Test
    @DisplayName("Обычный пользователь видит только свои, даже если просит чужие")
    void user_SeeOnlyOwn() {
      when(cardRepository.findByOwnerUserName("my_login", pageable)).thenReturn(Page.empty());

      cardService.getAllAvailableCards("my_login", "other_user", false, pageable);
      verify(cardRepository).findByOwnerUserName("my_login", pageable);
      verify(cardRepository, never()).findByOwnerUserName(eq("other_user"), any());
    }
  }
}
