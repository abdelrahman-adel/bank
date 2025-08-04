package com.bank.user;

import com.bank.user.config.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestContainersConfiguration.class)
@SpringBootTest
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
