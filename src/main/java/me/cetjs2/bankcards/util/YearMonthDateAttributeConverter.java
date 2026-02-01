package me.cetjs2.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Date;
import java.time.YearMonth;

@Converter(autoApply = true) // autoApply применит это ко всем YearMonth в проекте
public class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, Date> {

  @Override
  public Date convertToDatabaseColumn(YearMonth attribute) {
    return attribute == null ? null : Date.valueOf(attribute.atDay(1));
  }

  @Override
  public YearMonth convertToEntityAttribute(Date dbData) {
    return dbData == null ? null : YearMonth.from(dbData.toLocalDate());
  }
}
