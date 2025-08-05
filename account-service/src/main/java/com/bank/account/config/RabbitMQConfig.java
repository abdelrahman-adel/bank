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

    public static final String ACCOUNT_EVENTS_TOPIC_EXCHANGE = "account.events.topic";

    public static final String ACCOUNT_CREATED_ROUTING_KEY = "account.event.created";
    public static final String ACCOUNT_UPDATED_ROUTING_KEY = "account.event.updated";
    public static final String ACCOUNT_DELETED_ROUTING_KEY = "account.event.deleted";

    public static final String CUSTOMER_SERVICE_ACCOUNT_EVENTS_QUEUE = "customer.service.account.events.queue";

    @Bean
    public TopicExchange accountEventsTopicExchange() {
        return new TopicExchange(ACCOUNT_EVENTS_TOPIC_EXCHANGE);
    }

    @Bean
    public Queue customerServiceAccountEventsQueue() {
        return QueueBuilder.durable(CUSTOMER_SERVICE_ACCOUNT_EVENTS_QUEUE).build();
    }

    @Bean
    public Binding customerServiceBinding(TopicExchange accountEventsTopicExchange, Queue customerServiceAccountEventsQueue) {
        return BindingBuilder.bind(customerServiceAccountEventsQueue).to(accountEventsTopicExchange).with("account.event.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
