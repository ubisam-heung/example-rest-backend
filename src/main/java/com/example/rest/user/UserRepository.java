package com.example.rest.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @Query(value = """
    SELECT CASE
      WHEN EXISTS (SELECT 1 FROM users WHERE id = 1)
      THEN (
        SELECT MIN(t.id + 1)
        FROM users t
        LEFT JOIN users t2 ON t.id + 1 = t2.id
        WHERE t2.id IS NULL
      )
      ELSE 1
    END
    """, nativeQuery = true)
  Long findNextAvailableId();
}
