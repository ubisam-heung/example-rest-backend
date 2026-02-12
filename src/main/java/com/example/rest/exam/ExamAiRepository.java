package com.example.rest.exam;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamAiRepository extends JpaRepository<ExamAi, Long> {
	List<ExamAi> findByIsSolvedTrueAndIsCorrectFalse();

	List<ExamAi> findByCategoryAndIsSolvedTrueAndIsCorrectFalse(String category);
}
