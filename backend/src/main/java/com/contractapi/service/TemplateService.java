package com.contractapi.service;

import java.util.ArrayList;
import java.util.List;
import com.contractapi.entity.ContractTemplate;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
  private final List<ContractTemplate> templates = new ArrayList<>();

  public TemplateService() {
    ContractTemplate template = new ContractTemplate();
    template.setId(1L);
    template.setType("LEASE");
    template.setTitle("租赁合同");
    template.setContent("甲方：${partyA}\\n乙方：${partyB}\\n租金：${amount}\\n日期：${date}");
    template.setVariables("[\"partyA\",\"partyB\",\"amount\",\"date\"]");
    templates.add(template);
  }

  public List<ContractTemplate> list() { return templates; }
  public ContractTemplate create(ContractTemplate template) {
    template.setId(System.currentTimeMillis());
    templates.add(template);
    return template;
  }
  public ContractTemplate find(Long id) {
    return templates.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(templates.get(0));
  }
}
