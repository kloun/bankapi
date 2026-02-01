package me.cetjs2.bankcards.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "cards")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor // ОБЯЗАТЕЛЬНО для JPA
@AllArgsConstructor // ОБЯЗАТЕЛЬНО для Builder
public class Card {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "owner_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_cards_users"))
  private User owner;

  @Column(precision = 19, scale = 2) // Указываем точность для денег
  private BigDecimal balance;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private CardStatus cardStatus;

  @Column(name = "expiration_date", nullable = false)
  private YearMonth expirationDate;

  @Column(name = "card_number", nullable = false, unique = true)
  private String cardNumber;
}
