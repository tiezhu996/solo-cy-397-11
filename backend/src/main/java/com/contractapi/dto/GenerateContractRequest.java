package com.contractapi.dto;

import java.util.Map;

public record GenerateContractRequest(Long userId, Long templateId, String title, Map<String, String> variables, String format) {}
