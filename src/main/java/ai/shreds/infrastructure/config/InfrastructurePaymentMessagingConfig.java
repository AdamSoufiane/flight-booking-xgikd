package ai.shreds.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the payment service.
 * Sets up exchanges, queues, and message conversion.
 */
@Configuration
public class InfrastructurePaymentMessagingConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${payment.events.exchange}")
    private String paymentExchange;

    @Value("${payment.events.queue}")
    private String paymentQueue;

    @Value("${payment.events.routing-key-prefix}")
    private String routingKeyPrefix;

    /**
     * Configures the RabbitMQ connection factory
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    /**
     * Configures the RabbitMQ template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(paymentExchange);
        template.setChannelTransacted(true);
        return template;
    }

    /**
     * Configures the message converter for JSON
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Configures the payment events exchange
     */
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(paymentExchange, true, false);
    }

    /**
     * Configures the payment events queue
     */
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(paymentQueue)
                .withArgument("x-dead-letter-exchange", paymentExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "dead-letter")
                .build();
    }

    /**
     * Configures the binding between exchange and queue
     */
    @Bean
    public Binding paymentBinding() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange())
                .with(routingKeyPrefix + ".#");
    }

    /**
     * Configures the dead letter exchange
     */
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(paymentExchange + ".dlx", true, false);
    }

    /**
     * Configures the dead letter queue
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(paymentQueue + ".dlq").build();
    }

    /**
     * Configures the binding for dead letter queue
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("#");
    }
}
