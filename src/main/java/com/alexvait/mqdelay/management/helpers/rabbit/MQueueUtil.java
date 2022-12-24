package com.alexvait.mqdelay.management.helpers.rabbit;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper utility for creating of exchanges, queue configuration and binding queues to exchanges.
 */
public class MQueueUtil {
    private final Channel channel;

    public MQueueUtil(Channel channel) {
        this.channel = Objects.requireNonNull(channel, "RabbitMQ channel cannot be null");
    }

    public void createDirectExchange(String name) throws IOException {
        createExchange(name, BuiltinExchangeType.DIRECT);
    }

    public void createTopicExchange(String name) throws IOException {
        createExchange(name, BuiltinExchangeType.TOPIC);
    }

    private void createExchange(String name, BuiltinExchangeType type) throws IOException {
        channel.exchangeDeclare(name, type);
    }

    public void createAndBind(String queueName, String bindExchange, String deadLetterExchange, long ttl, String key) throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", deadLetterExchange);
        args.put("x-message-ttl", ttl);
        createAndBind(queueName, bindExchange, key, args);
    }

    public void createAndBind(String queueName, String bindExchange, String key) throws IOException {
        createAndBind(queueName, bindExchange, key, null);
    }

    protected void createAndBind(String queueName, String bindExchange, String key, Map<String, Object> args) throws IOException {
        channel.queueDeclare(queueName, false, false, false, args);
        channel.queueBind(queueName, bindExchange, key);
    }
}
