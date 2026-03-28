package com.company.authservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─── Exchange Names ────────────────────────────────────────
    public static final String AUTH_EXCHANGE       = "auth.exchange";
    public static final String TIMESHEET_EXCHANGE  = "timesheet.exchange";
    public static final String LEAVE_EXCHANGE      = "leave.exchange";

    // ─── Queue Names ───────────────────────────────────────────
    public static final String USER_REGISTERED_QUEUE     = "user.registered.queue";
    public static final String USER_ROLE_CHANGED_QUEUE   = "user.role.changed.queue";
    public static final String TIMESHEET_SUBMITTED_QUEUE = "timesheet.submitted.queue";
    public static final String TIMESHEET_APPROVED_QUEUE  = "timesheet.approved.queue";
    public static final String TIMESHEET_REJECTED_QUEUE  = "timesheet.rejected.queue";
    public static final String LEAVE_APPLIED_QUEUE       = "leave.applied.queue";
    public static final String LEAVE_APPROVED_QUEUE      = "leave.approved.queue";
    public static final String LEAVE_REJECTED_QUEUE      = "leave.rejected.queue";
    public static final String LEAVE_CANCELLED_QUEUE     = "leave.cancelled.queue";

    // ─── Routing Keys ──────────────────────────────────────────
    public static final String USER_REGISTERED_KEY     = "user.registered";
    public static final String USER_ROLE_CHANGED_KEY   = "user.role.changed";
    public static final String TIMESHEET_SUBMITTED_KEY = "timesheet.submitted";
    public static final String TIMESHEET_APPROVED_KEY  = "timesheet.approved";
    public static final String TIMESHEET_REJECTED_KEY  = "timesheet.rejected";
    public static final String LEAVE_APPLIED_KEY       = "leave.applied";
    public static final String LEAVE_APPROVED_KEY      = "leave.approved";
    public static final String LEAVE_REJECTED_KEY      = "leave.rejected";
    public static final String LEAVE_CANCELLED_KEY     = "leave.cancelled";

    // ─── Exchanges ─────────────────────────────────────────────
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE);
    }

    @Bean
    public TopicExchange timesheetExchange() {
        return new TopicExchange(TIMESHEET_EXCHANGE);
    }

    @Bean
    public TopicExchange leaveExchange() {
        return new TopicExchange(LEAVE_EXCHANGE);
    }
    // Removed Queues and Bindings since auth-service ONLY publishes events.

    // ─── JSON Message Converter ────────────────────────────────
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ─── RabbitTemplate ────────────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
