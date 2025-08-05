package com.bank.account;

import com.bank.customer.config.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestCustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(AccountServiceApplication::main).with(TestContainersConfiguration.class).run(args);
    }

}
