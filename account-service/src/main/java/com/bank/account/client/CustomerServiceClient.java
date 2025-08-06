package com.bank.account.client;

import com.bank.account.client.fallback.CustomerServiceClientFallback;
import com.bank.account.config.FeignClientConfig;
import com.bank.account.model.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "customer-service",
        url = "${customer.service.url}",
        fallback = CustomerServiceClientFallback.class,
        configuration = FeignClientConfig.class)
public interface CustomerServiceClient {

    @GetMapping("/api/v1/customer/search")
    CustomerDto getCustomerByLegalId(@RequestParam("legalId") String legalId);
}
