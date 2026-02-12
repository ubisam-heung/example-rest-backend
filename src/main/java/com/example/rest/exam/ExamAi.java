package com.example.rest.exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "exam_ai")
public class ExamAi {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_exam_id")
  private Exam sourceExam;

  @Column(nullable = false, length = 20)
  private String category;

  @Lob
  @Column(name = "question_text", columnDefinition = "LONGTEXT")
  private String questionText;

  @Lob
  @Column(name = "answer_text", columnDefinition = "LONGTEXT")
  private String answerText;

  @Lob
  @Column(name = "explanation", columnDefinition = "LONGTEXT")
  private String explanation;

  @Lob
  @Column(name = "user_answer", columnDefinition = "LONGTEXT")
  private String userAnswer;

  @Column(name = "is_solved", nullable = false)
  private boolean isSolved = false;

  @Column(name = "is_correct", nullable = false)
  private boolean isCorrect = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public ExamAi() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Exam getSourceExam() {
    return sourceExam;
  }

  public void setSourceExam(Exam sourceExam) {
    this.sourceExam = sourceExam;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getQuestionText() {
    return questionText;
  }

  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  public String getAnswerText() {
    return answerText;
  }

  public void setAnswerText(String answerText) {
    this.answerText = answerText;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public String getUserAnswer() {
    return userAnswer;
  }

  public void setUserAnswer(String userAnswer) {
    this.userAnswer = userAnswer;
  }

  public boolean isSolved() {
    return isSolved;
  }

  public void setSolved(boolean solved) {
    isSolved = solved;
  }

  public boolean isCorrect() {
    return isCorrect;
  }

  public void setCorrect(boolean correct) {
    isCorrect = correct;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
