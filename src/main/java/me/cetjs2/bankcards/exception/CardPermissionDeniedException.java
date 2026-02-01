package me.cetjs2.bankcards.exception;

public class CardPermissionDeniedException extends RuntimeException {

  public CardPermissionDeniedException(String message) {
    super(message);
  }

  public CardPermissionDeniedException() {
    super("You don't own this Card");
  }
}
