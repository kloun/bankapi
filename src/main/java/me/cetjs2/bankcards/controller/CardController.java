package me.cetjs2.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.dto.*;
import me.cetjs2.bankcards.service.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cards")
@Tag(name = "Карты", description = "Управление банковскими картами и операциями")
@SecurityRequirement(name = "bearerAuth") // Требовать токен для всех методов
public class CardController {
  private final CardService cardService;
  private final BlockRequestService blockRequestService;
  private final TransferService transferService;

  @Operation(
      summary = "Выпуск новой карты (Админ)",
      description = "Доступно только пользователям с ROLE_ADMIN")
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/new")
  public ResponseEntity<CardViewResponse> newCard(
      @Valid @RequestBody CreateCardRequest request, UriComponentsBuilder ucb) {
    var newCard = cardService.createCard(request);
    var location = ucb.path("/cards/view/{id}").buildAndExpand(newCard.id()).toUri();
    return ResponseEntity.created(location).body(newCard);
  }

  @Operation(summary = "Данные о карте")
  @GetMapping("/view/{id}")
  public ResponseEntity<CardViewResponse> getCardInfo(
      @Parameter(description = "ID карты") @PathVariable UUID id) {
    return ResponseEntity.ok(cardService.getCardInfo(id));
  }

  @Operation(summary = "Заблокировать карту (Админ)")
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/block/{id}")
  public ResponseEntity<SucessActionResponse> blockCard(@PathVariable UUID id) {
    return ResponseEntity.ok(cardService.blockCard(id));
  }

  @Operation(summary = "Удалить карту (Админ)")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<SucessActionResponse> deleteCard(@PathVariable UUID id) {
    return ResponseEntity.ok(cardService.deleteCard(id));
  }

  @Operation(
      summary = "Список доступных карт",
      description = "Админ видит всех или фильтрует по пользователю. Юзер видит только свои.")
  @GetMapping("/list")
  public ResponseEntity<Page<CardViewResponse>> getAll(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Логин (только для админа)") @RequestParam(required = false)
          String user,
      @ParameterObject Pageable pageable) {
    boolean isAdmin =
        userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    return ResponseEntity.ok(
        cardService.getAllAvailableCards(userDetails.getUsername(), user, isAdmin, pageable));
  }

  @Operation(
      summary = "Подать заявку на блокировку",
      description = "Создает запрос, который должен одобрить админ")
  @PostMapping("/sendblockrequest/{cardId}")
  public ResponseEntity<SucessActionResponse> sendBlockRequest(@PathVariable UUID cardId) {
    return ResponseEntity.ok(blockRequestService.sendBlockRequest(cardId));
  }

  @Operation(summary = "Перевод денег", description = "Между картами текущего пользователя")
  @PostMapping("/transfer")
  public ResponseEntity<SucessActionResponse> transfer(
      @Valid @RequestBody TransferRequest transferRequest) {
    return ResponseEntity.ok(transferService.transferMoney(transferRequest));
  }
}
