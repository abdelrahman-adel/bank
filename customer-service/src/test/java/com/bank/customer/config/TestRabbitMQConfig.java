package com.bank.customer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRabbitMQConfig {

    public static final String CUSTOMER_EVENTS_CONSUMER_QUEUE = "some.service.customer.events.queue";

    @Bean
    public Queue accountServiceCustomerEventsQueue() {
        return QueueBuilder.durable(CUSTOMER_EVENTS_CONSUMER_QUEUE).build();
    }

    @Bean
    public Binding accountServiceBinding(TopicExchange customerEventsTopicExchange) {
        return BindingBuilder.bind(accountServiceCustomerEventsQueue()).to(customerEventsTopicExchange).with("customer.event.*");
    }
}
