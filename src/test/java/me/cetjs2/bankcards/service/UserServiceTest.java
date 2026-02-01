package me.cetjs2.bankcards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import me.cetjs2.bankcards.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class UserServiceTest {

  private UserService userService;
  private SecurityContext securityContext;

  @BeforeEach
  void setUp() {
    userService = new UserService();
    securityContext = mock(SecurityContext.class);
    // Устанавливаем мок контекста в статический холдер
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  void tearDown() {
    // Очищаем контекст после каждого теста, чтобы не влиять на другие тесты
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Должен вернуть текущего пользователя, если он авторизован")
  void getCurrentUser_ShouldReturnUser_WhenAuthenticated() {
    // GIVEN
    User expectedUser = User.builder().userName("test_user").build();
    Authentication authentication = mock(Authentication.class);

    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(expectedUser);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    // WHEN
    User actualUser = userService.getCurrentUser();

    // THEN
    assertThat(actualUser).isNotNull();
    assertThat(actualUser.getUsername()).isEqualTo("test_user");
  }

  @Test
  @DisplayName("Должен вернуть null, если аутентификация отсутствует")
  void getCurrentUser_ShouldReturnNull_WhenNoAuthentication() {
    // GIVEN
    when(securityContext.getAuthentication()).thenReturn(null);

    // WHEN
    User actualUser = userService.getCurrentUser();

    // THEN
    assertThat(actualUser).isNull();
  }

  @Test
  @DisplayName("Должен вернуть null, если пользователь не прошел аутентификацию")
  void getCurrentUser_ShouldReturnNull_WhenNotAuthenticated() {
    // GIVEN
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    // WHEN
    User actualUser = userService.getCurrentUser();

    // THEN
    assertThat(actualUser).isNull();
  }
}
