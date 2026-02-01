package me.cetjs2.bankcards.repository;

import java.util.Optional;
import java.util.UUID;
import me.cetjs2.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUserName(String userName);
}
