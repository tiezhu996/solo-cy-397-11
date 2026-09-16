package com.contractapi.dto;

import java.util.List;
import com.contractapi.constants.TicketType;

public record TicketRequest(Long userId, TicketType type, String description, List<String> attachments) {}
