package me.cetjs2.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import me.cetjs2.bankcards.dto.AuthRequest;
import me.cetjs2.bankcards.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class) // Добавляем обработчик ошибок в контекст
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
    properties = {
      "spring.docker.compose.enabled=false",
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  // @Autowired private ObjectMapper objectMapper; // Теперь Spring найдет его автоматически

  // РЕШЕНИЕ: Инициализируем вручную, чтобы не зависеть от контекста
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private PasswordEncoder passwordEncoder;

  @MockitoBean
  private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

  @Test
  @DisplayName("Login: Успешный вход и получение токена")
  void login_Success() throws Exception {
    AuthRequest request = new AuthRequest("user", "password");
    String mockToken = "mocked-jwt-token";

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(jwtUtil.generateToken("user")).thenReturn(mockToken);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(mockToken));
  }

  @Test
  @DisplayName("Login: Ошибка 401 при неверных учетных данных")
  void login_BadCredentials() throws Exception {
    AuthRequest request = new AuthRequest("user", "wrong_password");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Invalid username or password"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid username or password"));
  }

  @Test
  @DisplayName("Login: Ошибка 400 при пустых полях (валидация)")
  void login_ValidationError() throws Exception {
    AuthRequest request = new AuthRequest("", "password");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
