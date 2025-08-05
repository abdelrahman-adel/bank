package com.bank.account.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRabbitMQConfig {

    public static final String ACCOUNT_EVENTS_CONSUMER_QUEUE = "some.service.account.events.queue";

    @Bean
    public Queue customerServiceAccountEventsQueue() {
        return QueueBuilder.durable(ACCOUNT_EVENTS_CONSUMER_QUEUE).build();
    }

    @Bean
    public Binding customerServiceBinding(TopicExchange accountEventsTopicExchange) {
        return BindingBuilder.bind(customerServiceAccountEventsQueue()).to(accountEventsTopicExchange).with("account.event.*");
    }
}
