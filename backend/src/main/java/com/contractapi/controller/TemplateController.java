package com.contractapi.controller;

import java.util.List;
import com.contractapi.entity.ContractTemplate;
import com.contractapi.service.TemplateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
  private final TemplateService service;
  public TemplateController(TemplateService service) { this.service = service; }
  @GetMapping public List<ContractTemplate> list() { return service.list(); }
  @PostMapping public ContractTemplate create(@RequestBody ContractTemplate template) { return service.create(template); }
}
