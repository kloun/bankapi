package me.cetjs2.bankcards.exception;

public class CardNotFoundException extends RuntimeException {

  public CardNotFoundException(String message) {
    super(message);
  }

  public CardNotFoundException() {
    super("Card not found");
  }
}
