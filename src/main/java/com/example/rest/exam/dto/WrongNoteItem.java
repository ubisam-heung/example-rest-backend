package com.example.rest.exam.dto;

public record WrongNoteItem(
    Long id,
    String category,
    String question_text,
    String answer_text,
    String user_answer,
    String explanation
) {
}
