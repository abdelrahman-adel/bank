package com.bank.account;

import com.bank.account.config.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestContainersConfiguration.class)
@SpringBootTest
class AccountServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}