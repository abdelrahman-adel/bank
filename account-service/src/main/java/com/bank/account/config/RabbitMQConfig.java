package com.bank.account.config;

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

    @Bean
    public TopicExchange accountEventsTopicExchange() {
        return new TopicExchange(ACCOUNT_EVENTS_TOPIC);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
