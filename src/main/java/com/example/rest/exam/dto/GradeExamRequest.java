package com.example.rest.exam.dto;

import java.util.List;

public record GradeExamRequest(
    List<GradeExamAnswer> answers
) {
}
