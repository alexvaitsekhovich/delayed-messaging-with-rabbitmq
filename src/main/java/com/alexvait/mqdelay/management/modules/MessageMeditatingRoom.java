package com.alexvait.mqdelay.management.modules;

import com.alexvait.mqdelay.management.helpers.NamingUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.alexvait.mqdelay.management.message.DelayedMessage;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * This class manages the messages from the upper-level queue. All messages, that have delay time
 * bigger than the longest delay in the binary-topic based queues, will be stored in the WAITING_HALL_EXCHANGE
 * and picked up by the WAITING_HALL_QUEUE. Then the message delay time will be decreased by the TTL of
 * the WAITING_HALL_QUEUE and if it is still longer than the longest delay, re-queued into the WAITING_HALL_EXCHANGE.
 * Otherwise the message will be provided with the binary-based topic and send into the highest exchange.
 */
public final class MessageMeditatingRoom implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MessageMeditatingRoom.class);

    private final Channel channel;
    private final long maxDelay;

    public MessageMeditatingRoom(Channel channel, int maxLevel) {
        if (maxLevel <= 0)
            throw new IllegalArgumentException("maximal level must be a positive number");

        this.channel = Objects.requireNonNull(channel, "Channel cannot be null");
        maxDelay = (long) Math.pow(2, maxLevel);
    }

    @Override
    public void run() {
        DeliverCallback deliverCallback = this::requeueOrDistribute;
        try {
            channel.basicConsume(RabbitConstants.PREPARING_QUEUE, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    // package private for testing
    void requeueOrDistribute(String unusedConsumerTag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

        logger.info("Received message: " + message);
        DelayedMessage delayedMessage = DelayedMessage.fromJsonString(message);

        if (delayedMessage.getDelayTime() >= maxDelay) {
            requeueToWait(delayedMessage);
        } else {
            sendToReceivingExchange(delayedMessage);
        }
    }

    private void requeueToWait(DelayedMessage delayedMessage) throws IOException {
        logger.info("Queue again to wait");

        delayedMessage.setDelayTime(delayedMessage.getDelayTime() - maxDelay);
        channel.basicPublish(RabbitConstants.WAITING_HALL_EXCHANGE, "", null,
                delayedMessage.toJsonString().getBytes(StandardCharsets.UTF_8));
    }

    private void sendToReceivingExchange(DelayedMessage delayedMessage) throws IOException {
        long seconds = delayedMessage.getDelayTime();

        var namingUtil = new NamingUtil(maxDelay);
        String topic = namingUtil.createTopicFromTtl(seconds) + "." + delayedMessage.getRoutingKey();

        String receivingExchange = namingUtil.getExchangeNameForTtl(maxDelay);

        logger.info(String.format("Redirecting message to exchange %s with topic %s", receivingExchange, topic));

        channel.basicPublish(receivingExchange, topic, null,
                delayedMessage.getPayload().getBytes(StandardCharsets.UTF_8));
    }
}
