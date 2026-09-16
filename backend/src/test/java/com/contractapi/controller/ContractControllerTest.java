package com.contractapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contractapi.constants.ContractStatus;
import com.contractapi.entity.Contract;
import com.contractapi.exception.ApiException;
import com.contractapi.exception.GlobalExceptionHandler;
import com.contractapi.service.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

  private MockMvc mockMvc;

  @Mock
  private ContractService service;

  @InjectMocks
  private ContractController controller;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  private Contract contract(String status) {
    Contract contract = new Contract();
    contract.setId(1L);
    contract.setUserId(1001L);
    contract.setTitle("测试合同");
    contract.setContent("合同正文");
    contract.setStatus(status);
    return contract;
  }

  @Test
  void withdrawEndpointReturnsWithdrawnContract() throws Exception {
    when(service.withdraw(eq(1L), eq(1001L)))
        .thenReturn(contract(ContractStatus.WITHDRAWN.name()));

    mockMvc.perform(post("/api/contracts/1/withdraw").param("userId", "1001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WITHDRAWN"))
        .andExpect(jsonPath("$.content").value("合同正文"));

    verify(service).withdraw(1L, 1001L);
  }

  @Test
  void withdrawByNonCreatorReturnsForbiddenBody() throws Exception {
    when(service.withdraw(any(), any()))
        .thenThrow(new ApiException("FORBIDDEN", "只有合同创建人可以撤回合同"));

    mockMvc.perform(post("/api/contracts/1/withdraw").param("userId", "2002"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
  }
}
