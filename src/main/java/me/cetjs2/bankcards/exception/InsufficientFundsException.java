package me.cetjs2.bankcards.exception;

public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException(String message) {
    super(message);
  }

  public InsufficientFundsException() {
    super("Insufficient funds for this transaction");
  }
}
