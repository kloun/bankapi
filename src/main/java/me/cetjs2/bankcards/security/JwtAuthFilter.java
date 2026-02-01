package me.cetjs2.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor // Автоматически создаст конструктор для final полей
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    // 1. Проверяем наличие заголовка Authorization
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    try {
      jwt = authHeader.substring(7);
      username = jwtUtil.extractUsername(jwt);
      // 2. Если имя пользователя извлечено и он еще не аутентифицирован в текущем контексте
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        // 3. Проверяем валидность токена (срок действия и соответствие пользователю)
        if (jwtUtil.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          // Добавляем детали запроса (IP, сессия) в объект аутентификации
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // 4. Устанавливаем пользователя в контекст безопасности
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
    }

    // Передаем управление следующему фильтру
    filterChain.doFilter(request, response);
  }
}
