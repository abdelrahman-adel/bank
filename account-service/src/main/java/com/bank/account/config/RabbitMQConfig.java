package com.bank.account.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ACCOUNT_EVENTS_TOPIC = "account.events.topic";
    public static final String ACCOUNT_CREATED_ROUTING_KEY = "account.event.created";
    public static final String ACCOUNT_UPDATED_ROUTING_KEY = "account.event.updated";
    public static final String ACCOUNT_DELETED_ROUTING_KEY = "account.event.deleted";

    public static final String ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE = "account.service.customer.events.queue";
    public static final String CUSTOMER_EVENTS_TOPIC = "customer.events.topic";

    @Bean
    public TopicExchange accountEventsTopicExchange() {
        return new TopicExchange(ACCOUNT_EVENTS_TOPIC);
    }

    @Bean
    public TopicExchange customerEventsTopicExchange() {
        return new TopicExchange(CUSTOMER_EVENTS_TOPIC);
    }

    @Bean
    public Queue accountServiceCustomerEventsQueue() {
        return QueueBuilder.durable(ACCOUNT_SERVICE_CUSTOMER_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding accountServiceBinding() {
        return BindingBuilder.bind(accountServiceCustomerEventsQueue()).to(customerEventsTopicExchange()).with("customer.event.deleted");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
