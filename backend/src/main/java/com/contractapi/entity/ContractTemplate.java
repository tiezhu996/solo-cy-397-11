package com.contractapi.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("contract_templates")
public class ContractTemplate {
  private Long id;
  private String type;
  private String title;
  private String content;
  private String variables;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public String getVariables() { return variables; }
  public void setVariables(String variables) { this.variables = variables; }
}
