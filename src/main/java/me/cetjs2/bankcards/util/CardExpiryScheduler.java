package me.cetjs2.bankcards.util;

import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.service.CardService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardExpiryScheduler {

  private final CardService cardService;

  @EventListener(ApplicationReadyEvent.class)
  @Scheduled(cron = "0 0 19 * * *")
  public void checkCardsExpiry() {
    cardService.blockExpiredCards();
  }
}
