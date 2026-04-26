package com.company.timesheetservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─── Exchange Names ────────────────────────────────────────
    public static final String AUTH_EXCHANGE       = "auth.exchange";
    public static final String TIMESHEET_EXCHANGE  = "timesheet.exchange";
    public static final String LEAVE_EXCHANGE      = "leave.exchange";

    // ─── Queue Names ───────────────────────────────────────────
    public static final String USER_REGISTERED_QUEUE     = "timesheet.user.registered.queue";
    public static final String USER_ROLE_CHANGED_QUEUE   = "timesheet.user.role.changed.queue";
    public static final String USER_DELETED_QUEUE        = "timesheet.user.deleted.queue";
    public static final String TIMESHEET_SUBMITTED_QUEUE = "timesheet.timesheet.submitted.queue";
    public static final String TIMESHEET_APPROVED_QUEUE  = "timesheet.timesheet.approved.queue";
    public static final String TIMESHEET_REJECTED_QUEUE  = "timesheet.timesheet.rejected.queue";
    public static final String LEAVE_APPLIED_QUEUE       = "timesheet.leave.applied.queue";
    public static final String LEAVE_APPROVED_QUEUE      = "timesheet.leave.approved.queue";
    public static final String LEAVE_REJECTED_QUEUE      = "timesheet.leave.rejected.queue";
    public static final String LEAVE_CANCELLED_QUEUE     = "timesheet.leave.cancelled.queue";

    // ─── Routing Keys ──────────────────────────────────────────
    public static final String USER_REGISTERED_KEY     = "user.registered";
    public static final String USER_ROLE_CHANGED_KEY   = "user.role.changed";
    public static final String USER_DELETED_KEY        = "user.deleted";
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

    // ─── Queues ────────────────────────────────────────────────
    @Bean public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }
    @Bean public Queue userRoleChangedQueue() {
        return QueueBuilder.durable(USER_ROLE_CHANGED_QUEUE).build();
    }
    @Bean public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE).build();
    }
    @Bean public Queue timesheetSubmittedQueue() {
        return QueueBuilder.durable(TIMESHEET_SUBMITTED_QUEUE).build();
    }
    @Bean public Queue timesheetApprovedQueue() {
        return QueueBuilder.durable(TIMESHEET_APPROVED_QUEUE).build();
    }
    @Bean public Queue timesheetRejectedQueue() {
        return QueueBuilder.durable(TIMESHEET_REJECTED_QUEUE).build();
    }
    @Bean public Queue leaveAppliedQueue() {
        return QueueBuilder.durable(LEAVE_APPLIED_QUEUE).build();
    }
    @Bean public Queue leaveApprovedQueue() {
        return QueueBuilder.durable(LEAVE_APPROVED_QUEUE).build();
    }
    @Bean public Queue leaveRejectedQueue() {
        return QueueBuilder.durable(LEAVE_REJECTED_QUEUE).build();
    }
    @Bean public Queue leaveCancelledQueue() {
        return QueueBuilder.durable(LEAVE_CANCELLED_QUEUE).build();
    }

    // ─── Bindings ──────────────────────────────────────────────
    @Bean public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(authExchange()).with(USER_REGISTERED_KEY);
    }
    @Bean public Binding userRoleChangedBinding() {
        return BindingBuilder.bind(userRoleChangedQueue())
                .to(authExchange()).with(USER_ROLE_CHANGED_KEY);
    }
    @Bean public Binding userDeletedBinding() {
        return BindingBuilder.bind(userDeletedQueue())
                .to(authExchange()).with(USER_DELETED_KEY);
    }
    @Bean public Binding timesheetSubmittedBinding() {
        return BindingBuilder.bind(timesheetSubmittedQueue())
                .to(timesheetExchange()).with(TIMESHEET_SUBMITTED_KEY);
    }
    @Bean public Binding timesheetApprovedBinding() {
        return BindingBuilder.bind(timesheetApprovedQueue())
                .to(timesheetExchange()).with(TIMESHEET_APPROVED_KEY);
    }
    @Bean public Binding timesheetRejectedBinding() {
        return BindingBuilder.bind(timesheetRejectedQueue())
                .to(timesheetExchange()).with(TIMESHEET_REJECTED_KEY);
    }
    @Bean public Binding leaveAppliedBinding() {
        return BindingBuilder.bind(leaveAppliedQueue())
                .to(leaveExchange()).with(LEAVE_APPLIED_KEY);
    }
    @Bean public Binding leaveApprovedBinding() {
        return BindingBuilder.bind(leaveApprovedQueue())
                .to(leaveExchange()).with(LEAVE_APPROVED_KEY);
    }
    @Bean public Binding leaveRejectedBinding() {
        return BindingBuilder.bind(leaveRejectedQueue())
                .to(leaveExchange()).with(LEAVE_REJECTED_KEY);
    }
    @Bean public Binding leaveCancelledBinding() {
        return BindingBuilder.bind(leaveCancelledQueue())
                .to(leaveExchange()).with(LEAVE_CANCELLED_KEY);
    }

    // ─── JSON Message Converter ────────────────────────────────
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // ─── RabbitTemplate ────────────────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
