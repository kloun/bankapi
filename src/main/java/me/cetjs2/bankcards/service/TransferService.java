package me.cetjs2.bankcards.service;

import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.dto.SucessActionResponse;
import me.cetjs2.bankcards.dto.TransferRequest;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.exception.CardNotFoundException;
import me.cetjs2.bankcards.exception.CardPermissionDeniedException;
import me.cetjs2.bankcards.exception.InsufficientFundsException;
import me.cetjs2.bankcards.repository.CardRepository;
import me.cetjs2.bankcards.util.CardUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TransferService {
  private final CardRepository cardRepository;
  private final CardUtil cardUtil;
  private final UserService userService;

  @Transactional
  public SucessActionResponse transferMoney(TransferRequest request) {
    Card fromCard =
        cardRepository
            .findById(request.fromCardId())
            .orElseThrow(() -> new CardNotFoundException());

    Card toCard =
        cardRepository.findById(request.toCardId()).orElseThrow(() -> new CardNotFoundException());

    String currentUserName = userService.getCurrentUser().getUsername();
    if (!cardRepository.existsByIdAndOwnerUserName(fromCard.getId(), currentUserName)
        || !cardRepository.existsByIdAndOwnerUserName(toCard.getId(), currentUserName)) {
      throw new CardPermissionDeniedException(
          "одна или несколько карт принадлежат другому пользователю");
    }
    if (fromCard.getCardStatus() != CardStatus.ACTIVE
        || toCard.getCardStatus() != CardStatus.ACTIVE) {
      throw new CardPermissionDeniedException("Карты просрочены или заблокированы");
    }
    if (request.sum().compareTo(fromCard.getBalance()) > 0) {
      throw new InsufficientFundsException();
    }
    Card updatedToCard = toCard.toBuilder().balance(toCard.getBalance().add(request.sum())).build();
    Card updatedFromCard =
        fromCard.toBuilder().balance(fromCard.getBalance().subtract(request.sum())).build();
    cardRepository.save(updatedToCard);
    cardRepository.save(updatedFromCard);
    return new SucessActionResponse(
        "сумма "
            + request.sum().toString()
            + " переведена с карты "
            + cardUtil.maskCardNumber(updatedFromCard.getCardNumber())
            + " на карту "
            + cardUtil.maskCardNumber(updatedToCard.getCardNumber()));
  }
}
