package com.contractapi.utils;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TemplateRenderer {
  public String render(String template, Map<String, String> variables) {
    String result = template;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      result = result.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return result;
  }
}
