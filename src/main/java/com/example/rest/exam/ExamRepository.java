package com.example.rest.exam;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamRepository extends JpaRepository<Exam, Long> {
	@Query(
			value = "SELECT * FROM exam WHERE (:category IS NULL OR category = :category) ORDER BY RAND()",
			nativeQuery = true
	)
	List<Exam> findRandomByCategory(@Param("category") String category, Pageable pageable);
}
