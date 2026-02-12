package com.example.rest.exam.dto;

public record GenerateExamRequest(
    String category,
    Integer count,
    Boolean save
) {
}
