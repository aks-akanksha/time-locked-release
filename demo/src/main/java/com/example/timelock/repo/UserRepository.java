package com.example.timelock.repo;

import com.example.timelock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmailAndActiveTrue(String email);
}
