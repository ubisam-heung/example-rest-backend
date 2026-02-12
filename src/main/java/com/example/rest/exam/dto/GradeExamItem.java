package com.example.rest.exam.dto;

public record GradeExamItem(
    Long id,
    boolean is_correct,
    String user_answer,
    String answer_text
) {
}
