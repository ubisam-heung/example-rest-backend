package com.example.rest.exam.dto;

import java.util.List;

public record WrongNoteResponse(
    List<WrongNoteItem> items
) {
}
