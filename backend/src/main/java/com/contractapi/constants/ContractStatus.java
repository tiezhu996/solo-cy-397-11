package com.contractapi.constants;

import java.util.Map;
import java.util.Set;

public enum ContractStatus {
  DRAFT, PENDING_SIGN, SIGNED, EXPIRED, WITHDRAWN;

  // 合法状态链路：草稿→待签；待签→签署/过期；签署→过期；过期、撤回为终态
  private static final Map<ContractStatus, Set<ContractStatus>> ALLOWED_TRANSITIONS = Map.of(
      DRAFT, Set.of(PENDING_SIGN),
      PENDING_SIGN, Set.of(SIGNED, EXPIRED),
      SIGNED, Set.of(EXPIRED),
      EXPIRED, Set.of(),
      WITHDRAWN, Set.of());

  public boolean canTransitionTo(ContractStatus target) {
    return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
  }
}
