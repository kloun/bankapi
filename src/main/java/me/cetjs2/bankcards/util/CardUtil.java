package me.cetjs2.bankcards.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class CardUtil {

  public String maskCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      return cardNumber; // Или обработка ошибки
    }
    return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
  }

  public String generateCardNumber(String iin) {
    SecureRandom random = new SecureRandom();

    // 1. Берем БИН (например, 444455)
    StringBuilder sb = new StringBuilder(iin);

    // 2. Генерируем 9 случайных цифр
    for (int i = 0; i < 9; i++) {
      sb.append(random.nextInt(10));
    }

    // 3. Вычисляем и добавляем 16-ю цифру (контрольную)
    String partialCardNumber = sb.toString();
    String checkDigit = calculateLuhnDigit(partialCardNumber);

    return partialCardNumber + checkDigit;
  }

  private String calculateLuhnDigit(String number) {
    int sum = 0;
    number = number.replaceAll("\\D", "");
    boolean alternate = true;

    // Идем справа налево
    for (int i = number.length() - 1; i >= 0; i--) {
      int n = Integer.parseInt(number.substring(i, i + 1));
      if (alternate) {
        n *= 2;
        if (n > 9) n -= 9;
      }
      sum += n;
      alternate = !alternate;
    }

    int lastDigit = (10 - (sum % 10)) % 10;
    return String.valueOf(lastDigit);
  }
}
