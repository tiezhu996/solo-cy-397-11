package com.contractapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.contractapi.mapper")
public class ContractApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(ContractApiApplication.class, args);
  }
}
