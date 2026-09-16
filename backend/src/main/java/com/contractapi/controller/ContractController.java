package com.contractapi.controller;

import java.util.List;
import com.contractapi.constants.ContractStatus;
import com.contractapi.dto.GenerateContractRequest;
import com.contractapi.entity.Contract;
import com.contractapi.service.ContractService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
  private final ContractService service;
  public ContractController(ContractService service) { this.service = service; }
  @PostMapping("/generate") public Contract generate(@RequestBody GenerateContractRequest request) { return service.generate(request); }
  @PatchMapping("/{id}/status") public Contract updateStatus(@PathVariable Long id, @RequestParam ContractStatus status) { return service.updateStatus(id, status); }
  @GetMapping public List<Contract> list(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status) { return service.list(userId, status); }
  @PostMapping("/{id}/pdf") public String exportPdf(@PathVariable Long id) { return service.exportPdf(id); }
}
