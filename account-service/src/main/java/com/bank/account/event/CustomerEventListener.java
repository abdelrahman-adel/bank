package com.bank.account.event;

import com.bank.account.config.RabbitMQConfig;
import com.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final AccountService accountService;

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE)
    public void handleCustomerDeletedEvent(Long customerId) {
        log.info("Received customer deleted event for customer ID: {}", customerId);
        try {
            accountService.deleteAccountsByCustomerId(customerId);
        } catch (Exception e) {
            log.error("Error processing customer deleted event for customer ID: {}. Error: {}", customerId, e.getMessage());
        }
    }
}