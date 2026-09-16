package com.contractapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.contractapi.constants.TicketStatus;
import com.contractapi.dto.TicketRequest;
import com.contractapi.entity.LegalTicket;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
  private final List<LegalTicket> tickets = new ArrayList<>();
  private final List<Map<String, Object>> replies = new ArrayList<>();

  public LegalTicket create(TicketRequest request) {
    LegalTicket ticket = new LegalTicket();
    ticket.setId(System.currentTimeMillis());
    ticket.setUserId(request.userId());
    ticket.setType(request.type().name());
    ticket.setDescription(request.description());
    ticket.setStatus(TicketStatus.PENDING.name());
    ticket.setAttachments(String.valueOf(request.attachments()));
    tickets.add(ticket);
    return ticket;
  }

  public LegalTicket updateStatus(Long id, TicketStatus status) {
    LegalTicket ticket = tickets.stream().filter(item -> item.getId().equals(id)).findFirst().orElseThrow();
    ticket.setStatus(status.name());
    return ticket;
  }

  public Map<String, Object> reply(Long id, String content, List<String> attachments) {
    Map<String, Object> reply = Map.of("ticketId", id, "content", content, "attachments", attachments);
    replies.add(reply);
    updateStatus(id, TicketStatus.REPLIED);
    return reply;
  }
}
