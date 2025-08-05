package com.bank.account.client;

import com.bank.account.model.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${customer.service.url}")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);
}
