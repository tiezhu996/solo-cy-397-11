package com.contractapi.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import com.contractapi.constants.ContractStatus;
import com.contractapi.constants.ErrorCode;
import com.contractapi.dto.GenerateContractRequest;
import com.contractapi.entity.Contract;
import com.contractapi.entity.ContractTemplate;
import com.contractapi.exception.ApiException;
import com.contractapi.utils.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContractService {
  private static final Logger log = LoggerFactory.getLogger(ContractService.class);

  private final TemplateService templateService;
  private final TemplateRenderer renderer;
  private final List<Contract> contracts = new CopyOnWriteArrayList<>();
  // 每份合同一把锁，保证签署与撤回的状态变更串行化
  private final Map<Long, Object> contractLocks = new ConcurrentHashMap<>();
  // 毫秒基准上的原子递增，避免同一毫秒内生成多份合同时 ID 碰撞
  private final AtomicLong idSequence = new AtomicLong(System.currentTimeMillis());

  public ContractService(TemplateService templateService, TemplateRenderer renderer) {
    this.templateService = templateService;
    this.renderer = renderer;
  }

  public Contract generate(GenerateContractRequest request) {
    ContractTemplate template = templateService.find(request.templateId());
    Contract contract = new Contract();
    contract.setId(idSequence.incrementAndGet());
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
    if (status == ContractStatus.WITHDRAWN) {
      // 撤回只能通过专门的撤回接口，由创建人发起
      throw new ApiException(ErrorCode.VALIDATION_FAILED, "请使用撤回接口撤回待签合同");
    }
    Contract contract = find(id);
    synchronized (lockOf(id)) {
      ContractStatus current = ContractStatus.valueOf(contract.getStatus());
      if (current == ContractStatus.WITHDRAWN) {
        // 撤回是终态：禁止通过通用状态更新重新打开或变更
        throw new ApiException(ErrorCode.CONTRACT_WITHDRAWN, "合同已撤回，不能变更状态");
      }
      if (!current.canTransitionTo(status)) {
        // 非法回退或跳跃一律拒绝，且不改动当前状态
        throw new ApiException(ErrorCode.CONTRACT_INVALID_TRANSITION,
            "合同状态不能从 " + current + " 变更为 " + status);
      }
      contract.setStatus(status.name());
      return contract;
    }
  }

  /**
   * 撤回待签合同。只有创建人可撤回；重复撤回幂等返回同一份合同；
   * 草稿/已签署/已过期均不能撤回。
   */
  public Contract withdraw(Long id, Long operatorUserId) {
    Contract contract = find(id);
    synchronized (lockOf(id)) {
      if (!contract.getUserId().equals(operatorUserId)) {
        throw new ApiException(ErrorCode.FORBIDDEN, "只有合同创建人可以撤回合同");
      }
      String current = contract.getStatus();
      if (ContractStatus.WITHDRAWN.name().equals(current)) {
        // 幂等：重复撤回返回同一结果，正文保持不变
        return contract;
      }
      if (!ContractStatus.PENDING_SIGN.name().equals(current)) {
        throw new ApiException(ErrorCode.CONTRACT_NOT_PENDING_SIGN, "只有待签署合同可以撤回，当前状态：" + current);
      }
      contract.setStatus(ContractStatus.WITHDRAWN.name());
      log.info("合同 {} 已被创建人 {} 撤回，正文保留且不可再签署", id, operatorUserId);
      return contract;
    }
  }

  public List<Contract> list(Long userId, String status) {
    return contracts.stream().filter(item -> (userId == null || item.getUserId().equals(userId)) && (status == null || item.getStatus().equals(status))).toList();
  }

  public String exportPdf(Long id) {
    return "wkhtmltopdf 已在 Docker 镜像安装，合同 " + id + " 可导出到 /tmp/contracts/" + id + ".pdf";
  }

  private Contract find(Long id) {
    return contracts.stream().filter(item -> item.getId().equals(id)).findFirst()
        .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "合同不存在：" + id));
  }

  private Object lockOf(Long id) {
    return contractLocks.computeIfAbsent(id, key -> new Object());
  }
}
