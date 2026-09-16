package com.contractapi.service;

import java.util.List;
import com.contractapi.entity.LegalFaq;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeService {
  private final List<LegalFaq> faqs;

  public KnowledgeService() {
    LegalFaq faq = new LegalFaq();
    faq.setId(1L);
    faq.setCategory("合同纠纷");
    faq.setQuestion("合同签署后能否撤销？");
    faq.setAnswer("需结合撤销事由、证据与法定期限判断。");
    this.faqs = List.of(faq);
  }

  public List<LegalFaq> search(String keyword) {
    if (keyword == null || keyword.isBlank()) return faqs;
    return faqs.stream().filter(item -> item.getQuestion().contains(keyword) || item.getAnswer().contains(keyword)).toList();
  }
}
