package com.example.rest.exam.dto;

import java.util.List;

public record GradeExamResponse(
    List<GradeExamItem> items
) {
}
