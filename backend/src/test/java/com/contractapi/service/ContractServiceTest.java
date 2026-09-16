package com.contractapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.contractapi.constants.ContractStatus;
import com.contractapi.dto.GenerateContractRequest;
import com.contractapi.entity.Contract;
import com.contractapi.exception.ApiException;
import com.contractapi.utils.TemplateRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContractServiceTest {

  private ContractService service;

  @BeforeEach
  void setUp() {
    service = new ContractService(new TemplateService(), new TemplateRenderer());
  }

  private Contract createContract(Long userId) {
    return service.generate(new GenerateContractRequest(
        userId, 1L, "测试合同", Map.of("partyA", "张三", "partyB", "李四"), "TEXT"));
  }

  private Contract pendingContract(Long userId) throws InterruptedException {
    Contract contract = createContract(userId);
    service.updateStatus(contract.getId(), ContractStatus.PENDING_SIGN);
    return contract;
  }

  @Test
  void creatorCanWithdrawPendingContractAndContentIsKept() throws Exception {
    Contract contract = pendingContract(1001L);
    String content = contract.getContent();

    Contract withdrawn = service.withdraw(contract.getId(), 1001L);

    assertSame(contract, withdrawn);
    assertEquals(ContractStatus.WITHDRAWN.name(), withdrawn.getStatus());
    // 正文保留
    assertEquals(content, withdrawn.getContent());
  }

  @Test
  void nonCreatorCannotWithdraw() throws Exception {
    Contract contract = pendingContract(1001L);

    ApiException ex = assertThrows(ApiException.class,
        () -> service.withdraw(contract.getId(), 2002L));
    assertEquals("FORBIDDEN", ex.getCode());
    assertEquals(ContractStatus.PENDING_SIGN.name(), contract.getStatus());
  }

  @Test
  void duplicateWithdrawReturnsSameResult() throws Exception {
    Contract contract = pendingContract(1001L);

    Contract first = service.withdraw(contract.getId(), 1001L);
    Contract second = service.withdraw(contract.getId(), 1001L);

    assertSame(first, second);
    assertEquals(ContractStatus.WITHDRAWN.name(), second.getStatus());
  }

  @Test
  void draftSignedExpiredCannotBeWithdrawn() throws Exception {
    Contract draft = createContract(1001L);
    ApiException draftEx = assertThrows(ApiException.class,
        () -> service.withdraw(draft.getId(), 1001L));
    assertEquals("CONTRACT_NOT_PENDING_SIGN", draftEx.getCode());

    Contract signed = pendingContract(1001L);
    service.updateStatus(signed.getId(), ContractStatus.SIGNED);
    ApiException signedEx = assertThrows(ApiException.class,
        () -> service.withdraw(signed.getId(), 1001L));
    assertEquals("CONTRACT_NOT_PENDING_SIGN", signedEx.getCode());

    Contract expired = pendingContract(1001L);
    service.updateStatus(expired.getId(), ContractStatus.EXPIRED);
    ApiException expiredEx = assertThrows(ApiException.class,
        () -> service.withdraw(expired.getId(), 1001L));
    assertEquals("CONTRACT_NOT_PENDING_SIGN", expiredEx.getCode());
  }

  @Test
  void withdrawnContractCanNoLongerBeSigned() throws Exception {
    Contract contract = pendingContract(1001L);
    service.withdraw(contract.getId(), 1001L);

    ApiException ex = assertThrows(ApiException.class,
        () -> service.updateStatus(contract.getId(), ContractStatus.SIGNED));
    assertEquals("CONTRACT_WITHDRAWN", ex.getCode());
    assertEquals(ContractStatus.WITHDRAWN.name(), contract.getStatus());
  }

  @Test
  void withdrawMissingContractThrowsNotFound() {
    ApiException ex = assertThrows(ApiException.class,
        () -> service.withdraw(999999L, 1001L));
    assertEquals("NOT_FOUND", ex.getCode());
  }

  @Test
  void genericStatusUpdateCannotSetWithdrawn() throws Exception {
    Contract contract = pendingContract(1001L);
    ApiException ex = assertThrows(ApiException.class,
        () -> service.updateStatus(contract.getId(), ContractStatus.WITHDRAWN));
    assertEquals("VALIDATION_FAILED", ex.getCode());
    assertEquals(ContractStatus.PENDING_SIGN.name(), contract.getStatus());
  }

  @Test
  void signAndWithdrawArrivingTogetherOnlyOneWins() throws Exception {
    // 20 份合同，每份同时发起一次签署与一次撤回，终态必须为 SIGNED 或 WITHDRAWN 之一
    int rounds = 20;
    Long[] ids = new Long[rounds];
    for (int i = 0; i < rounds; i++) {
      // generate 以毫秒时间戳为 ID，等待 2ms 保证每份合同 ID 唯一
      Thread.sleep(2);
      ids[i] = pendingContract(1001L).getId();
    }

    ExecutorService pool = Executors.newFixedThreadPool(8);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(rounds * 2);
    AtomicInteger signed = new AtomicInteger();
    AtomicInteger withdrawn = new AtomicInteger();

    for (Long id : ids) {
      pool.submit(() -> {
        try {
          start.await();
          service.updateStatus(id, ContractStatus.SIGNED);
          signed.incrementAndGet();
        } catch (ApiException expected) {
          // 撤回先生效时，签署失败且不得覆盖终态
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          done.countDown();
        }
      });
      pool.submit(() -> {
        try {
          start.await();
          service.withdraw(id, 1001L);
          withdrawn.incrementAndGet();
        } catch (ApiException expected) {
          // 签署先生效时，撤回失败（已签署不能撤回）
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          done.countDown();
        }
      });
    }

    start.countDown();
    assertTrue(done.await(10, TimeUnit.SECONDS), "并发任务未在超时前完成");
    pool.shutdown();

    for (Long id : ids) {
      Contract latest = service.list(null, null).stream()
          .filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
      String status = latest.getStatus();
      assertTrue(
          ContractStatus.SIGNED.name().equals(status) || ContractStatus.WITHDRAWN.name().equals(status),
          "终态被非法覆盖：" + status);
    }
    // 每份合同恰好一方成功，没有任何终态被双方覆盖
    assertEquals(rounds, signed.get() + withdrawn.get());
  }
}
