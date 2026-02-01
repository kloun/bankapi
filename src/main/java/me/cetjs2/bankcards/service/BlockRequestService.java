package me.cetjs2.bankcards.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.dto.SucessActionResponse;
import me.cetjs2.bankcards.entity.BlockRequest;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.exception.CardNotFoundException;
import me.cetjs2.bankcards.exception.CardPermissionDeniedException;
import me.cetjs2.bankcards.repository.BlockRequestRepository;
import me.cetjs2.bankcards.repository.CardRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BlockRequestService {
  private final BlockRequestRepository blockRequestRepository;
  private final CardRepository cardRepository;
  private final UserService userService;

  public SucessActionResponse sendBlockRequest(UUID cardId) {
    Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException());
    if (card.getOwner() != userService.getCurrentUser()) {
      throw new CardPermissionDeniedException();
    }
    if (blockRequestRepository.existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(cardId)) {
      return new SucessActionResponse(
          "Запрос на блокировку карты существует в БД,  ожидайте решения");
    }
    if (card.getCardStatus() != CardStatus.ACTIVE) {
      throw new CardPermissionDeniedException("Карта просрочена или уже заблокирована");
    }
    BlockRequest blockRequest =
        BlockRequest.builder().card(card).createdDate(LocalDateTime.now()).build();
    blockRequestRepository.save(blockRequest);
    return new SucessActionResponse("Запрос на блокировку карты отправлен");
  }
}
