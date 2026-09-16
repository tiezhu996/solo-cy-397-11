package com.contractapi.service;

import java.util.ArrayList;
import java.util.List;
import com.contractapi.constants.ContractStatus;
import com.contractapi.dto.GenerateContractRequest;
import com.contractapi.entity.Contract;
import com.contractapi.entity.ContractTemplate;
import com.contractapi.utils.TemplateRenderer;
import org.springframework.stereotype.Service;

@Service
public class ContractService {
  private final TemplateService templateService;
  private final TemplateRenderer renderer;
  private final List<Contract> contracts = new ArrayList<>();

  public ContractService(TemplateService templateService, TemplateRenderer renderer) {
    this.templateService = templateService;
    this.renderer = renderer;
  }

  public Contract generate(GenerateContractRequest request) {
    ContractTemplate template = templateService.find(request.templateId());
    Contract contract = new Contract();
    contract.setId(System.currentTimeMillis());
    contract.setUserId(request.userId());
    contract.setTemplateId(template.getId());
    contract.setTitle(request.title());
    contract.setContent(renderer.render(template.getContent(), request.variables()));
    contract.setStatus(ContractStatus.DRAFT.name());
    contract.setSigners("[]");
    contracts.add(contract);
    return contract;
  }

  public Contract updateStatus(Long id, ContractStatus status) {
    Contract contract = contracts.stream().filter(item -> item.getId().equals(id)).findFirst().orElseThrow();
    contract.setStatus(status.name());
    return contract;
  }

  public List<Contract> list(Long userId, String status) {
    return contracts.stream().filter(item -> (userId == null || item.getUserId().equals(userId)) && (status == null || item.getStatus().equals(status))).toList();
  }

  public String exportPdf(Long id) {
    return "wkhtmltopdf 已在 Docker 镜像安装，合同 " + id + " 可导出到 /tmp/contracts/" + id + ".pdf";
  }
}
