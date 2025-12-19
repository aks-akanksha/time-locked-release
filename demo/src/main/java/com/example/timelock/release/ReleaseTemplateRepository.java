package com.example.timelock.release;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseTemplateRepository extends JpaRepository<ReleaseTemplate, Long> {
  List<ReleaseTemplate> findByActiveTrue();
  Optional<ReleaseTemplate> findByNameAndActiveTrue(String name);
}

