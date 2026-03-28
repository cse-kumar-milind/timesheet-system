package com.company.authservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.company.authservice.config.RabbitMQConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        try {
            rabbitTemplate.convertAndSend(AUTH_EXCHANGE, USER_REGISTERED_KEY, event);
            log.info("Published user.registered event for: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish user.registered event: {}", e.getMessage());
        }
    }

    public void publishUserRoleChanged(UserRegisteredEvent event) {
        try {
            rabbitTemplate.convertAndSend(AUTH_EXCHANGE, USER_ROLE_CHANGED_KEY, event);
            log.info("Published user.role.changed event for: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish user.role.changed event: {}", e.getMessage());
        }
    }
}