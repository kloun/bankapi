package me.cetjs2.bankcards.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.dto.CardViewResponse;
import me.cetjs2.bankcards.dto.CreateCardRequest;
import me.cetjs2.bankcards.dto.SucessActionResponse;
import me.cetjs2.bankcards.entity.BlockRequest;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.entity.User;
import me.cetjs2.bankcards.exception.CardNotFoundException;
import me.cetjs2.bankcards.exception.UsernameNotFoundException;
import me.cetjs2.bankcards.repository.BlockRequestRepository;
import me.cetjs2.bankcards.repository.CardRepository;
import me.cetjs2.bankcards.repository.UserRepository;
import me.cetjs2.bankcards.util.CardUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {
  private final CardRepository cardRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final BlockRequestRepository blockRequestRepository;
  private final CardUtil cardUtil;

  public CardViewResponse createCard(CreateCardRequest createCardRequest) {
    User ownerCard =
        userRepository
            .findByUserName(createCardRequest.ownerUsername())
            .orElseThrow(() -> new UsernameNotFoundException());
    Card newCard =
        Card.builder()
            .owner(ownerCard)
            .balance(createCardRequest.initialBalance())
            .cardStatus(CardStatus.ACTIVE)
            .expirationDate(createCardRequest.expirationDate())
            .cardNumber(cardUtil.generateCardNumber("4444 4"))
            .build();
    newCard = cardRepository.save(newCard);
    return new CardViewResponse(
        newCard.getId(),
        cardUtil.maskCardNumber(newCard.getCardNumber()),
        ownerCard.getUsername(),
        newCard.getCardStatus(),
        newCard.getExpirationDate(),
        newCard.getBalance());
  }

  public CardViewResponse getCardInfo(UUID id) {
    Card card =
        cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
    return new CardViewResponse(
        card.getId(),
        cardUtil.maskCardNumber(card.getCardNumber()),
        card.getOwner().getUsername(),
        card.getCardStatus(),
        card.getExpirationDate(),
        card.getBalance());
  }

  @Transactional
  public SucessActionResponse blockCard(UUID id) {
    Card card =
        cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
    card = card.toBuilder().cardStatus(CardStatus.BLOCKED).build();
    cardRepository.save(card);
    if (blockRequestRepository.existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(id)) {
      BlockRequest blockRequest =
          blockRequestRepository.findOneByCardIdAndApprovedByIsNullAndApprovedDateIsNull(id);
      blockRequest =
          blockRequest.toBuilder()
              .approvedBy(userService.getCurrentUser())
              .approvedDate(LocalDateTime.now())
              .build();
      blockRequestRepository.save(blockRequest);
    }
    return new SucessActionResponse("Карта " + id.toString() + " успешно заблокирована");
  }

  public SucessActionResponse deleteCard(UUID id) {

    if (!cardRepository.existsById(id)) {
      throw new CardNotFoundException();
    }
    cardRepository.deleteById(id);

    return new SucessActionResponse("Карта " + id.toString() + " успешно удалена");
  }

  public Page<CardViewResponse> getAllAvailableCards(
      String currentUsername, String targetUsername, boolean isAdmin, Pageable pageable) {
    Page<Card> cardPage;

    if (isAdmin) {
      // Админ может фильтровать по конкретному юзеру или смотреть всех
      if (targetUsername != null && !targetUsername.isBlank()) {
        cardPage = cardRepository.findByOwnerUserName(targetUsername, pageable);
      } else {
        cardPage = cardRepository.findAll(pageable);
      }
    } else {
      // Обычный юзер видит ТОЛЬКО свои карты
      cardPage = cardRepository.findByOwnerUserName(currentUsername, pageable);
    }

    return cardPage.map(
        card ->
            new CardViewResponse(
                card.getId(),
                cardUtil.maskCardNumber(card.getCardNumber()),
                card.getOwner().getUsername(),
                card.getCardStatus(),
                card.getExpirationDate(),
                card.getBalance()));
  }

  @Transactional
  public void blockExpiredCards() {
    YearMonth today = YearMonth.now();
    // Находим все активные карты, у которых дата истечения меньше текущего месяца
    List<Card> expiredCards =
        cardRepository.findAllByCardStatusAndExpirationDateBefore(CardStatus.ACTIVE, today);

    for (Card card : expiredCards) {
      var expiredCard = card.toBuilder().cardStatus(CardStatus.EXPIRED).build();
      cardRepository.save(expiredCard);
    }
  }
}
