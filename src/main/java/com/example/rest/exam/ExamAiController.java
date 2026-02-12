package com.example.rest.exam;

import com.example.rest.exam.dto.GenerateExamRequest;
import com.example.rest.exam.dto.GenerateExamResponse;
import com.example.rest.exam.dto.GradeExamRequest;
import com.example.rest.exam.dto.GradeExamResponse;
import com.example.rest.exam.dto.StartExamRequest;
import com.example.rest.exam.dto.StartExamResponse;
import com.example.rest.exam.dto.WrongNoteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exam-ai")
public class ExamAiController {
  private final ExamAiService examAiService;

  public ExamAiController(ExamAiService examAiService) {
    this.examAiService = examAiService;
  }

  @PostMapping("/generate")
  public ResponseEntity<GenerateExamResponse> generate(@RequestBody GenerateExamRequest request) {
    return ResponseEntity.ok(examAiService.generate(request));
  }

  @PostMapping("/session")
  public ResponseEntity<StartExamResponse> startSession(@RequestBody StartExamRequest request) {
    return ResponseEntity.ok(examAiService.startSession(request));
  }

  @PostMapping("/grade")
  public ResponseEntity<GradeExamResponse> grade(@RequestBody GradeExamRequest request) {
    return ResponseEntity.ok(examAiService.grade(request));
  }

  @GetMapping("/wrong")
  public ResponseEntity<WrongNoteResponse> wrongNotes(@RequestParam(required = false) String category) {
    return ResponseEntity.ok(examAiService.getWrongNotes(category));
  }
}
