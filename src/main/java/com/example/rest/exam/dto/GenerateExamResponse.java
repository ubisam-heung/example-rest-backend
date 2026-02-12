package com.example.rest.exam.dto;

import java.util.List;

public record GenerateExamResponse(
    List<GeneratedExamItem> items
) {
}
