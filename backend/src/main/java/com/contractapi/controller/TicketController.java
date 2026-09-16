package com.contractapi.controller;

import java.util.List;
import java.util.Map;
import com.contractapi.constants.TicketStatus;
import com.contractapi.dto.TicketRequest;
import com.contractapi.entity.LegalTicket;
import com.contractapi.service.TicketService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
  private final TicketService service;
  public TicketController(TicketService service) { this.service = service; }
  @PostMapping public LegalTicket create(@RequestBody TicketRequest request) { return service.create(request); }
  @PatchMapping("/{id}/status") public LegalTicket status(@PathVariable Long id, @RequestParam TicketStatus status) { return service.updateStatus(id, status); }
  @PostMapping("/{id}/replies") public Map<String, Object> reply(@PathVariable Long id, @RequestBody Map<String, Object> body) { return service.reply(id, String.valueOf(body.get("content")), (List<String>) body.getOrDefault("attachments", List.of())); }
}
