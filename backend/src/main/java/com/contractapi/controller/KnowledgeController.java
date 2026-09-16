package com.contractapi.controller;

import java.util.List;
import com.contractapi.entity.LegalFaq;
import com.contractapi.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
  private final KnowledgeService service;
  public KnowledgeController(KnowledgeService service) { this.service = service; }
  @GetMapping public List<LegalFaq> search(@RequestParam(required = false) String keyword) { return service.search(keyword); }
}
