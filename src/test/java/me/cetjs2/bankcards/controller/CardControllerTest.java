package me.cetjs2.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;
import me.cetjs2.bankcards.dto.*;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.service.*;
import me.cetjs2.bankcards.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CardController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
    properties = {
      "spring.docker.compose.enabled=false",
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
class CardControllerTest {

  @Autowired private MockMvc mockMvc;

  // Ручной ObjectMapper гарантирует работу в SB 4 без ошибок контекста
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @MockitoBean private CardService cardService;
  @MockitoBean private BlockRequestService blockRequestService;
  @MockitoBean private TransferService transferService;

  // Необходимые заглушки для того, чтобы контекст безопасности не падал при старте
  @MockitoBean private UserDetailsService userDetailsService;
  @MockitoBean private JwtUtil jwtUtil;

  @Test
  @DisplayName("GET /cards/view/{id} - Проверка всех полей ответа")
  void getCardInfo_CheckAllFields() throws Exception {
    UUID id = UUID.randomUUID();
    CardViewResponse response =
        new CardViewResponse(
            id,
            "4444 4*** 1111",
            "ivan_gold",
            CardStatus.ACTIVE,
            YearMonth.of(2028, 12),
            new BigDecimal("1500.50"));

    when(cardService.getCardInfo(id)).thenReturn(response);

    mockMvc
        .perform(get("/cards/view/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.cardNumber").value("4444 4*** 1111"))
        .andExpect(jsonPath("$.expirationDate").value("12/28"))
        .andExpect(jsonPath("$.balance").value(1500.50));
  }

  @Test
  @DisplayName("POST /cards/new - Успешное создание карты")
  void createCard_Success() throws Exception {
    CreateCardRequest request =
        new CreateCardRequest("admin_user", BigDecimal.ZERO, YearMonth.now().plusYears(1));
    CardViewResponse response =
        new CardViewResponse(
            UUID.randomUUID(),
            "mask",
            "admin_user",
            CardStatus.ACTIVE,
            YearMonth.now(),
            BigDecimal.ZERO);

    when(cardService.createCard(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/cards/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));
  }

  @Test
  @DisplayName("POST /cards/transfer - Перевод денег")
  void transfer_Success() throws Exception {
    TransferRequest request =
        new TransferRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"));
    when(transferService.transferMoney(any()))
        .thenReturn(new SucessActionResponse("Перевод выполнен"));

    mockMvc
        .perform(
            post("/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Перевод выполнен"));
  }

  @Test
  @DisplayName("POST /cards/new - Ошибка валидации (дата в прошлом)")
  void createCard_InvalidDate() throws Exception {
    CreateCardRequest request =
        new CreateCardRequest("user", BigDecimal.TEN, YearMonth.now().minusMonths(1));

    mockMvc
        .perform(
            post("/cards/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("DELETE /cards/delete/{id} - Удаление карты")
  void deleteCard_Success() throws Exception {
    UUID id = UUID.randomUUID();
    when(cardService.deleteCard(id)).thenReturn(new SucessActionResponse("Удалена"));

    mockMvc
        .perform(delete("/cards/delete/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Удалена"));
  }
}
