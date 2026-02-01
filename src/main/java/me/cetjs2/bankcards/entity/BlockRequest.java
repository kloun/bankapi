package me.cetjs2.bankcards.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "block_requests")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BlockRequest {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_blockrequests_users"))
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "card_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_blockrequests_cards"))
  private Card card;

  @Column(name = "created_date", nullable = false)
  private LocalDateTime createdDate;

  @Column(name = "approved_date")
  private LocalDateTime approvedDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "approved_by",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_blockrequests_users2"))
  private User approvedBy;
}
