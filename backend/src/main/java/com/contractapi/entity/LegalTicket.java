package com.contractapi.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("legal_tickets")
public class LegalTicket {
  private Long id;
  private Long userId;
  private String type;
  private String description;
  private String status;
  private String attachments;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getAttachments() { return attachments; }
  public void setAttachments(String attachments) { this.attachments = attachments; }
}
