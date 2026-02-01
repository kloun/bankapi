package me.cetjs2.bankcards.repository;

import java.util.List;
import java.util.UUID;
import me.cetjs2.bankcards.entity.BlockRequest;
import me.cetjs2.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRequestRepository extends JpaRepository<BlockRequest, UUID> {

  List<BlockRequest> findByUser(User user);

  boolean existsByCardIdAndApprovedByIsNullAndApprovedDateIsNull(UUID cardId);

  BlockRequest findOneByCardIdAndApprovedByIsNullAndApprovedDateIsNull(UUID cardId);
}
