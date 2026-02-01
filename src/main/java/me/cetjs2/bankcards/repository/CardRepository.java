package me.cetjs2.bankcards.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import me.cetjs2.bankcards.entity.Card;
import me.cetjs2.bankcards.entity.CardStatus;
import me.cetjs2.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
  List<Card> findByOwner(User owner);

  // List<Card> findByOwnerUserName(String username);

  List<Card> findAllByCardStatusAndExpirationDateBefore(CardStatus status, YearMonth date);

  boolean existsByIdAndOwnerUserName(UUID cardId, String userName);

  Page<Card> findByOwnerUserName(String username, Pageable pageable);

  Page<Card> findAll(Pageable pageable);
}
