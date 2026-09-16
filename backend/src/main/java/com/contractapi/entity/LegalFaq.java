package com.contractapi.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("legal_faq")
public class LegalFaq {
  private Long id;
  private String category;
  private String question;
  private String answer;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getQuestion() { return question; }
  public void setQuestion(String question) { this.question = question; }
  public String getAnswer() { return answer; }
  public void setAnswer(String answer) { this.answer = answer; }
}
