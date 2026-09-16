package com.contractapi.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("contracts")
public class Contract {
  private Long id;
  private Long userId;
  private Long templateId;
  private String title;
  private String content;
  private String status;
  private String signers;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getTemplateId() { return templateId; }
  public void setTemplateId(Long templateId) { this.templateId = templateId; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getSigners() { return signers; }
  public void setSigners(String signers) { this.signers = signers; }
}
