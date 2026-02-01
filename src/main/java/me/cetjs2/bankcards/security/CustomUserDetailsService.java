package me.cetjs2.bankcards.security;

import lombok.RequiredArgsConstructor;
import me.cetjs2.bankcards.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    var user =
        userRepository
            .findByUserName(userName)
            .orElseThrow(
                () -> new UsernameNotFoundException("Пользователь не найден: " + userName));
    System.out.println("Password from DB: " + user.getPassword());
    return user;
  }
}
