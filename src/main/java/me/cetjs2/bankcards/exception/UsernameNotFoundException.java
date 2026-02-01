package me.cetjs2.bankcards.exception;

public class UsernameNotFoundException extends RuntimeException {
  public UsernameNotFoundException(String message) {
    super(message);
  }

  public UsernameNotFoundException() {
    super("User not found");
  }
}
