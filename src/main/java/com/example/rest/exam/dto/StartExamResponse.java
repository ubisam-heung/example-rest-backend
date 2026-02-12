package com.example.rest.exam.dto;

import java.util.List;

public record StartExamResponse(
    List<ExamAiQuestionItem> items
) {
}
