package com.forensic.case.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String BATCH_ANALYSIS_QUEUE = "batch.analysis.queue";
    public static final String BATCH_ANALYSIS_PRIORITY_QUEUE = "batch.analysis.priority.queue";
    public static final String BATCH_ANALYSIS_RESULT_QUEUE = "batch.analysis.result.queue";
    public static final String BATCH_ANALYSIS_CANCEL_QUEUE = "batch.analysis.cancel.queue";

    // Exchange names
    public static final String BATCH_ANALYSIS_EXCHANGE = "batch.analysis.exchange";
    public static final String BATCH_ANALYSIS_RESULT_EXCHANGE = "batch.analysis.result.exchange";

    // Routing keys
    public static final String BATCH_ANALYSIS_ROUTING_KEY = "batch.analysis";
    public static final String BATCH_ANALYSIS_PRIORITY_ROUTING_KEY = "batch.analysis.priority";
    public static final String BATCH_ANALYSIS_RESULT_ROUTING_KEY = "batch.analysis.result";
    public static final String BATCH_ANALYSIS_CANCEL_ROUTING_KEY = "batch.analysis.cancel";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Exchanges
    @Bean
    public TopicExchange batchAnalysisExchange() {
        return new TopicExchange(BATCH_ANALYSIS_EXCHANGE);
    }

    @Bean
    public TopicExchange batchAnalysisResultExchange() {
        return new TopicExchange(BATCH_ANALYSIS_RESULT_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue batchAnalysisQueue() {
        return QueueBuilder.durable(BATCH_ANALYSIS_QUEUE)
                .withArgument("x-max-priority", 10)
                .build();
    }

    @Bean
    public Queue batchAnalysisPriorityQueue() {
        return QueueBuilder.durable(BATCH_ANALYSIS_PRIORITY_QUEUE)
                .withArgument("x-max-priority", 10)
                .build();
    }

    @Bean
    public Queue batchAnalysisResultQueue() {
        return QueueBuilder.durable(BATCH_ANALYSIS_RESULT_QUEUE).build();
    }

    @Bean
    public Queue batchAnalysisCancelQueue() {
        return QueueBuilder.durable(BATCH_ANALYSIS_CANCEL_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding batchAnalysisBinding() {
        return BindingBuilder
                .bind(batchAnalysisQueue())
                .to(batchAnalysisExchange())
                .with(BATCH_ANALYSIS_ROUTING_KEY);
    }

    @Bean
    public Binding batchAnalysisPriorityBinding() {
        return BindingBuilder
                .bind(batchAnalysisPriorityQueue())
                .to(batchAnalysisExchange())
                .with(BATCH_ANALYSIS_PRIORITY_ROUTING_KEY);
    }

    @Bean
    public Binding batchAnalysisResultBinding() {
        return BindingBuilder
                .bind(batchAnalysisResultQueue())
                .to(batchAnalysisResultExchange())
                .with(BATCH_ANALYSIS_RESULT_ROUTING_KEY);
    }

    @Bean
    public Binding batchAnalysisCancelBinding() {
        return BindingBuilder
                .bind(batchAnalysisCancelQueue())
                .to(batchAnalysisExchange())
                .with(BATCH_ANALYSIS_CANCEL_ROUTING_KEY);
    }
}
