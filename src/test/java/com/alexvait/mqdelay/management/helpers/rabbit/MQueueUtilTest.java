package com.alexvait.mqdelay.management.helpers.rabbit;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;

@DisplayName("Test the invocation utility for exchanges and queues")
class MQueueUtilTest {

    private final String queueName = "test-queue";
    private final String exchangeName = "test-exchange";
    private final String dlExchangeName = "dead-letter-test-exchange";
    private final String routingKey = "test-key";

    @Mock
    private Channel channel;
    private MQueueUtil mQueueUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mQueueUtil = new MQueueUtil(channel);
    }

    @Test
    @DisplayName("Test create of a queue and binding to an exchange #1")
    public void testCreateAndBind1() throws IOException {
        long randTtl = new Random(System.currentTimeMillis()).nextInt(10000);
        mQueueUtil.createAndBind(queueName, exchangeName, dlExchangeName, randTtl, routingKey);

        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlExchangeName);
        args.put("x-message-ttl", randTtl);

        assertAll(
                () -> verify(channel).queueDeclare(queueName, false, false, false, args),
                () -> verify(channel).queueBind(queueName, exchangeName, routingKey)
        );
    }

    @Test
    @DisplayName("Test create of a queue and binding to an exchange #2")
    public void testCreateAndBind2() throws IOException {
        mQueueUtil.createAndBind(queueName, exchangeName, routingKey);

        assertAll(
                () -> verify(channel).queueDeclare(queueName, false, false, false, null),
                () -> verify(channel).queueBind(queueName, exchangeName, routingKey)
        );
    }

    @Test
    @DisplayName("Test create of a queue and binding to an exchange #3")
    public void testCreateAndBind3() throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("someValue", "TEST");

        mQueueUtil.createAndBind(queueName, exchangeName, routingKey, args);

        assertAll(
                () -> verify(channel).queueDeclare(queueName, false, false, false, args),
                () -> verify(channel).queueBind(queueName, exchangeName, routingKey)
        );
    }

    @Test
    @DisplayName("Test creation of direct exchange")
    public void testCreateDirectExchange() throws IOException {
        mQueueUtil.createDirectExchange(exchangeName);
        verify(channel).exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT);
    }

    @Test
    @DisplayName("Test creation of topic exchange")
    public void testCreateTopicExchange() throws IOException {
        mQueueUtil.createTopicExchange(exchangeName);
        verify(channel).exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
    }

}