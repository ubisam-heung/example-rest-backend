package com.example.rest.exam;

import com.example.rest.exam.dto.ExamAiQuestionItem;
import com.example.rest.exam.dto.GenerateExamRequest;
import com.example.rest.exam.dto.GenerateExamResponse;
import com.example.rest.exam.dto.GeneratedExamItem;
import com.example.rest.exam.dto.GradeExamAnswer;
import com.example.rest.exam.dto.GradeExamItem;
import com.example.rest.exam.dto.GradeExamRequest;
import com.example.rest.exam.dto.GradeExamResponse;
import com.example.rest.exam.dto.StartExamRequest;
import com.example.rest.exam.dto.StartExamResponse;
import com.example.rest.exam.dto.WrongNoteItem;
import com.example.rest.exam.dto.WrongNoteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExamAiService {
  private static final int DEFAULT_COUNT = 5;
  private static final int MAX_COUNT = 10;
  private static final int EXAMPLE_COUNT = 6;
  private static final int SESSION_COUNT = 5;

  private final ExamRepository examRepository;
  private final ExamAiRepository examAiRepository;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String baseUrl;
  private final String model;

  public ExamAiService(
      ExamRepository examRepository,
      ExamAiRepository examAiRepository,
      RestTemplateBuilder restTemplateBuilder,
      ObjectMapper objectMapper,
      @Value("${app.ollama.base-url}") String baseUrl,
      @Value("${app.ollama.model}") String model
  ) {
    this.examRepository = examRepository;
    this.examAiRepository = examAiRepository;
    this.restTemplate = restTemplateBuilder.build();
    this.objectMapper = objectMapper;
    this.baseUrl = baseUrl;
    this.model = model;
  }

  public GenerateExamResponse generate(GenerateExamRequest request) {
    String category = normalizeCategory(request.category());
    int count = normalizeCount(request.count());
    boolean save = request.save() != null && request.save();

    List<Exam> examples = examRepository.findRandomByCategory(category, PageRequest.of(0, EXAMPLE_COUNT));
    if (examples.isEmpty()) {
      throw new IllegalStateException("예시 문제를 찾을 수 없습니다.");
    }

    String prompt = buildPrompt(examples, category, count);
    String responseText = callOllama(prompt);
    List<GeneratedExamItem> items = parseItems(responseText);

    if (save && !items.isEmpty()) {
      saveItems(items, category, examples.get(0).getCategory());
    }

    return new GenerateExamResponse(items);
  }

  public StartExamResponse startSession(StartExamRequest request) {
    String category = normalizeCategory(request.category());
    boolean coding = isCodingCategory(category);
    int count = SESSION_COUNT;

    List<Exam> examples = findExamples(category);
    if (examples.isEmpty()) {
      throw new IllegalStateException("예시 문제를 찾을 수 없습니다.");
    }

    String prompt = coding
        ? buildCodingPrompt(examples, category, count)
        : buildTheoryPrompt(examples, category, count);
    String responseText = callOllama(prompt);
    List<GeneratedExamItem> items = parseItems(responseText);
    if (items.size() > count) {
      items = items.subList(0, count);
    }

    if (items.isEmpty()) {
      throw new IllegalStateException("생성된 문제가 없습니다.");
    }

    List<ExamAi> saved = saveItems(items, category, examples.get(0).getCategory());
    List<ExamAiQuestionItem> responseItems = new ArrayList<>();
    for (ExamAi examAi : saved) {
      responseItems.add(new ExamAiQuestionItem(
          examAi.getId(),
          examAi.getCategory(),
          examAi.getQuestionText()
      ));
    }

    return new StartExamResponse(responseItems);
  }

  public GradeExamResponse grade(GradeExamRequest request) {
    if (request.answers() == null || request.answers().isEmpty()) {
      throw new IllegalStateException("제출할 답안이 없습니다.");
    }

    List<Long> ids = new ArrayList<>();
    for (GradeExamAnswer answer : request.answers()) {
      if (answer.id() != null) {
        ids.add(answer.id());
      }
    }

    List<ExamAi> rows = examAiRepository.findAllById(ids);
    Map<Long, ExamAi> rowMap = new HashMap<>();
    for (ExamAi row : rows) {
      rowMap.put(row.getId(), row);
    }

    List<ExamAi> toSave = new ArrayList<>();
    List<GradeExamItem> resultItems = new ArrayList<>();

    for (GradeExamAnswer answer : request.answers()) {
      ExamAi row = rowMap.get(answer.id());
      if (row == null) {
        continue;
      }

      String userAnswer = normalizeAnswer(answer.user_answer());
      if (userAnswer.isBlank()) {
        throw new IllegalStateException("답을 채워주세요.");
      }

      String expected = normalizeAnswer(row.getAnswerText());
      boolean correct = userAnswer.equals(expected);

      row.setUserAnswer(answer.user_answer());
      row.setSolved(true);
      row.setCorrect(correct);
      toSave.add(row);

      resultItems.add(new GradeExamItem(
          row.getId(),
          correct,
          answer.user_answer(),
          row.getAnswerText()
      ));
    }

    if (!toSave.isEmpty()) {
      examAiRepository.saveAll(toSave);
    }

    return new GradeExamResponse(resultItems);
  }

  public WrongNoteResponse getWrongNotes(String category) {
    String normalizedCategory = normalizeCategory(category);
    List<ExamAi> rows = normalizedCategory == null
        ? examAiRepository.findByIsSolvedTrueAndIsCorrectFalse()
        : examAiRepository.findByCategoryAndIsSolvedTrueAndIsCorrectFalse(normalizedCategory);

    List<WrongNoteItem> items = new ArrayList<>();
    for (ExamAi row : rows) {
      items.add(new WrongNoteItem(
          row.getId(),
          row.getCategory(),
          row.getQuestionText(),
          row.getAnswerText(),
          row.getUserAnswer(),
          row.getExplanation()
      ));
    }

    return new WrongNoteResponse(items);
  }

  private String normalizeCategory(String category) {
    if (category == null || category.isBlank()) {
      return null;
    }
    return category.trim();
  }

  private int normalizeCount(Integer count) {
    if (count == null || count < 1) {
      return DEFAULT_COUNT;
    }
    return Math.min(count, MAX_COUNT);
  }

  private boolean isCodingCategory(String category) {
    if (category == null) {
      return false;
    }
    String value = category.trim().toLowerCase();
    return value.equals("c") || value.equals("java") || value.equals("python");
  }

  private List<Exam> findExamples(String category) {
    if (category != null) {
      List<Exam> examples = examRepository.findRandomByCategory(category, PageRequest.of(0, EXAMPLE_COUNT));
      if (!examples.isEmpty()) {
        return examples;
      }
    }
    return examRepository.findRandomByCategory(null, PageRequest.of(0, EXAMPLE_COUNT));
  }

  private String buildPrompt(List<Exam> examples, String category, int count) {
    StringBuilder builder = new StringBuilder();
    builder.append("너는 정보처리기사 스타일의 문제를 생성하는 AI다.\n");
    builder.append("아래 예시는 스타일 참고용이며 문장을 복사하지 마라.\n");
    builder.append("출력은 JSON 배열만 반환하고, 각 항목은 다음 키를 가진다: category, question_text, answer_text, explanation.\n");
    builder.append("explanation은 한 줄로 간단히 무조건 한국어로만 작성한다.\n");
    builder.append("동일한 내용이나 문구를 반복하지 말 것.\n\n");
    builder.append("응답은 JSON 배열만 출력한다. 다른 텍스트는 절대 포함하지 마라.\n");
    builder.append("반드시 ").append(count).append("개 항목을 출력한다.\n");
    builder.append("예시 형식: [{\"category\":\"Theory\",\"question_text\":\"...\",\"answer_text\":\"...\",\"explanation\":\"...\"}]\n\n");

    if (category != null) {
      builder.append("요청 카테고리: ").append(category).append("\n\n");
    } else {
      builder.append("카테고리는 자유롭게 선택 가능.\n\n");
    }

    builder.append("[예시]\n");
    for (Exam exam : examples) {
      builder
          .append("- [")
          .append(exam.getCategory())
          .append("] ")
          .append(exam.getQuestionText().replace("\n", " "))
          .append(" => ")
          .append(exam.getAnswerText())
          .append("\n");
    }

    builder.append("\n");
    builder.append("생성 개수: ").append(count).append("\n");
    builder.append("JSON 배열만 출력하라.\n");
    return builder.toString();
  }

  private String buildCodingPrompt(List<Exam> examples, String category, int count) {
    String language = category == null ? "C/Java/Python" : category;
    StringBuilder builder = new StringBuilder();
    builder.append("너는 정보처리기사 스타일의 코딩 문제를 생성하는 AI다.\n");
    builder.append("언어는 ").append(language).append(" 이다.\n");
    builder.append("각 문제는 코드가 포함되어야 하고, 질문은 반드시 출력 결과를 묻는 형태여야 한다.\n");
    builder.append("answer_text는 코드 실행 시 정확한 출력만 작성한다.\n");
    builder.append("출력은 JSON 배열만 반환하고, 각 항목은 다음 키를 가진다: category, question_text, answer_text, explanation.\n");
    builder.append("explanation은 한 줄로 간단히 무조건 한국어로만 작성한다.\n");
    builder.append("문장을 복사하지 말고 새로운 문제를 만들 것.\n\n");
    builder.append("응답은 JSON 배열만 출력한다. 다른 텍스트는 절대 포함하지 마라.\n");
    builder.append("반드시 ").append(count).append("개 항목을 출력한다.\n");
    builder.append("예시 형식: [{\"category\":\"C\",\"question_text\":\"...\",\"answer_text\":\"...\",\"explanation\":\"...\"}]\n\n");

    builder.append("[예시]\n");
    for (Exam exam : examples) {
      builder
          .append("- [")
          .append(exam.getCategory())
          .append("] ")
          .append(exam.getQuestionText().replace("\n", " "))
          .append(" => ")
          .append(exam.getAnswerText())
          .append("\n");
    }

    builder.append("\n");
    builder.append("생성 개수: ").append(count).append("\n");
    builder.append("question_text에는 코드와 함께 '출력 결과를 작성하시오.'를 포함하라.\n");
    builder.append("JSON 배열만 출력하라.\n");
    return builder.toString();
  }

  private String buildTheoryPrompt(List<Exam> examples, String category, int count) {
    StringBuilder builder = new StringBuilder();
    builder.append("너는 정보처리기사 스타일의 이론 문제를 생성하는 AI다.\n");
    builder.append("정의나 설명을 주고 무엇인지 맞히는 문제를 만든다.\n");
    builder.append("서브넷 마스크 계산처럼 간단한 계산형 문제도 포함 가능하다.\n");
    builder.append("출력은 JSON 배열만 반환하고, 각 항목은 다음 키를 가진다: category, question_text, answer_text, explanation.\n");
    builder.append("explanation은 한 줄로 간단히 무조건 한국어로만 작성한다.\n");
    builder.append("문장을 복사하지 말고 새로운 문제를 만들 것.\n\n");
    builder.append("응답은 JSON 배열만 출력한다. 다른 텍스트는 절대 포함하지 마라.\n");
    builder.append("반드시 ").append(count).append("개 항목을 출력한다.\n");
    builder.append("예시 형식: [{\"category\":\"Theory\",\"question_text\":\"...\",\"answer_text\":\"...\",\"explanation\":\"...\"}]\n\n");

    if (category != null) {
      builder.append("요청 카테고리: ").append(category).append("\n\n");
    }

    builder.append("[예시]\n");
    for (Exam exam : examples) {
      builder
          .append("- [")
          .append(exam.getCategory())
          .append("] ")
          .append(exam.getQuestionText().replace("\n", " "))
          .append(" => ")
          .append(exam.getAnswerText())
          .append("\n");
    }

    builder.append("\n");
    builder.append("생성 개수: ").append(count).append("\n");
    builder.append("JSON 배열만 출력하라.\n");
    return builder.toString();
  }

  private String callOllama(String prompt) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> body = new HashMap<>();
    body.put("model", model);
    body.put("prompt", prompt);
    body.put("stream", false);
    body.put("format", buildJsonSchema());
    body.put("options", Map.of("temperature", 0.2));

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
    OllamaGenerateResponse response = restTemplate.postForObject(
        baseUrl + "/api/generate",
        entity,
        OllamaGenerateResponse.class
    );

    if (response == null || response.response() == null) {
      throw new IllegalStateException("Ollama 응답이 비어 있습니다.");
    }

    return response.response();
  }

  private List<GeneratedExamItem> parseItems(String text) {
    String json = extractJsonPayload(text);
    try {
      JsonNode root = objectMapper.readTree(json);
      List<GeneratedExamItem> items = new ArrayList<>();
      if (root.isArray()) {
        for (JsonNode node : root) {
          if (node.isObject()) {
            items.add(objectMapper.treeToValue(node, GeneratedExamItem.class));
          }
        }
      } else if (root.isObject()) {
        JsonNode arr = root.get("items");
        if (arr != null && arr.isArray()) {
          for (JsonNode node : arr) {
            if (node.isObject()) {
              items.add(objectMapper.treeToValue(node, GeneratedExamItem.class));
            }
          }
        }
      }

      if (items.isEmpty()) {
        throw new IllegalStateException("JSON 배열에 유효한 문제가 없습니다.");
      }
      return items;
    } catch (Exception ex) {
      throw new IllegalStateException("JSON 파싱 실패: " + ex.getMessage(), ex);
    }
  }

  private Map<String, Object> buildJsonSchema() {
    Map<String, Object> schema = new HashMap<>();
    schema.put("type", "array");

    Map<String, Object> item = new HashMap<>();
    item.put("type", "object");

    Map<String, Object> properties = new HashMap<>();
    properties.put("category", Map.of("type", "string"));
    properties.put("question_text", Map.of("type", "string"));
    properties.put("answer_text", Map.of("type", "string"));
    properties.put("explanation", Map.of("type", "string"));

    item.put("properties", properties);
    item.put("required", List.of("category", "question_text", "answer_text", "explanation"));

    schema.put("items", item);
    return schema;
  }

  private String extractJsonPayload(String text) {
    int arrayStart = text.indexOf('[');
    int objectStart = text.indexOf('{');
    int start;
    if (arrayStart >= 0 && objectStart >= 0) {
      start = Math.min(arrayStart, objectStart);
    } else if (arrayStart >= 0) {
      start = arrayStart;
    } else {
      start = objectStart;
    }

    if (start < 0) {
      throw new IllegalStateException("JSON 데이터를 찾지 못했습니다.");
    }

    char startChar = text.charAt(start);
    int end = startChar == '[' ? text.lastIndexOf(']') : text.lastIndexOf('}');
    if (end <= start) {
      throw new IllegalStateException("JSON 끝 위치를 찾지 못했습니다.");
    }
    return text.substring(start, end + 1);
  }

  private List<ExamAi> saveItems(List<GeneratedExamItem> items, String requestCategory, String fallbackCategory) {
    List<ExamAi> entities = new ArrayList<>();
    for (GeneratedExamItem item : items) {
      if (item.question_text() == null || item.question_text().isBlank()) {
        continue;
      }
      if (item.answer_text() == null || item.answer_text().isBlank()) {
        continue;
      }
      String explanation = item.explanation();
      if (explanation == null || explanation.isBlank()) {
        explanation = "정답: " + item.answer_text();
      }
      String category = requestCategory != null ? requestCategory : normalizeCategory(item.category());
      if (category == null) {
        category = fallbackCategory;
      }
      ExamAi examAi = new ExamAi();
      examAi.setCategory(category);
      examAi.setQuestionText(item.question_text());
      examAi.setAnswerText(item.answer_text());
      examAi.setExplanation(explanation);
      entities.add(examAi);
    }
    if (entities.isEmpty()) {
      return List.of();
    }
    return examAiRepository.saveAll(entities);
  }

  private String normalizeAnswer(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\r\n", "\n").trim();
  }

  private record OllamaGenerateResponse(String response) {
  }
}
