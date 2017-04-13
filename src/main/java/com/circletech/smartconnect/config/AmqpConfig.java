package com.circletech.smartconnect.config;

import com.circletech.smartconnect.network.AmqpMessageController;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by xieyingfei on 2016/12/17.//RabbitMQ configuration
 */
@Configuration
public class AmqpConfig {
    @Value("${spring.rabbitmq.addresses}")
    private String addresses;
    @Value("${spring.rabbitmq.username}")
    private String mquser;
    @Value("${spring.rabbitmq.password}")
    private String mqpwd;


    @Value("${amqp.exchange.name}")
    private String exchangeName;
    @Value("${amqp.queue.name}")
    private String queueName;

    @Value("${amqp.maxconcurrent.consumers}")
    private int maxConcurrentConsumers;
    @Value("${amqp.concurrent.consumers}")
    private int concurrentConsumers;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(addresses);
        connectionFactory.setUsername(mquser);
        connectionFactory.setPassword(mqpwd);
        connectionFactory.setPublisherConfirms(true);//must config
        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)//must prototype type
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        return template;
    }

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(maxConcurrentConsumers);
        container.setConcurrentConsumers(concurrentConsumers);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    AmqpMessageController receiver(RabbitTemplate rabbitTemplate) {
        AmqpMessageController amqpMessageController = new AmqpMessageController(rabbitTemplate);
        amqpMessageController.constructor(exchangeName);
        return amqpMessageController;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(AmqpMessageController receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
}
