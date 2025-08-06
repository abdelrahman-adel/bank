package com.bank.account.client.fallback;


import com.bank.account.client.CustomerServiceClient;
import com.bank.account.exception.SystemException;
import com.bank.account.model.dto.CustomerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceClientFallback implements CustomerServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceClientFallback.class);

    @Override
    public CustomerDto getCustomerByLegalId(String legalId) {
        log.error("Customer service is down. Fallback for legalId: {}", legalId);
        throw new SystemException("Customer service is currently unavailable. Please try again later.");
    }
}
