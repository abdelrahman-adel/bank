package com.bank.customer.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CUSTOMER_EVENTS_TOPIC = "customer.events.topic";

    public static final String CUSTOMER_CREATED_ROUTING_KEY = "customer.event.created";
    public static final String CUSTOMER_UPDATED_ROUTING_KEY = "customer.event.updated";
    public static final String CUSTOMER_DELETED_ROUTING_KEY = "customer.event.deleted";

    @Bean
    public TopicExchange customerEventsTopicExchange() {
        return new TopicExchange(CUSTOMER_EVENTS_TOPIC);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
