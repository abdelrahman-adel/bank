package com.bank.user;

import com.bank.user.config.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(UserServiceApplication::main).with(TestContainersConfiguration.class).run(args);
	}

}
