package com.javaevaluation.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // 队列名称
    public static final String TASK_QUEUE = "evaluation.task.queue";
    public static final String RESULT_QUEUE = "evaluation.result.queue";

    // 交换机名称
    public static final String EXCHANGE = "evaluation.exchange";

    // 路由键
    public static final String TASK_ROUTING_KEY = "evaluation.task";
    public static final String RESULT_ROUTING_KEY = "evaluation.result";

    /**
     * 消息转换器（JSON格式）
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * 交换机
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * 任务队列（持久化，消息TTL 10分钟）
     */
    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable(TASK_QUEUE)
                .withArgument("x-message-ttl", 600000)
                .build();
    }

    /**
     * 结果队列（持久化）
     */
    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(RESULT_QUEUE).build();
    }

    /**
     * 任务队列绑定交换机
     */
    @Bean
    public Binding taskBinding() {
        return BindingBuilder.bind(taskQueue())
                .to(exchange())
                .with(TASK_ROUTING_KEY);
    }

    /**
     * 结果队列绑定交换机
     */
    @Bean
    public Binding resultBinding() {
        return BindingBuilder.bind(resultQueue())
                .to(exchange())
                .with(RESULT_ROUTING_KEY);
    }
}