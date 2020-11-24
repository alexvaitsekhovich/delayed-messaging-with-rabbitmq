package com.alexvait.mqdelay.management.modules;

import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * This class creates the pickup exchange, where all messages with expired delay time will land.
 * From this exchange the distribution queue will get messages, provide them with routing key
 * for their destination and send only the main json message.
 */
public final class MessageDistributor implements Runnable {
    private final Channel channel;
    private static final Logger logger = LoggerFactory.getLogger(MessageDistributor.class);

    public MessageDistributor(Channel channel) {
        this.channel = Objects.requireNonNull(channel, "Channel cannot be null");
    }

    @Override
    public void run() {
        DeliverCallback deliverCallback = this::distributeMessage;

        try {
            channel.basicConsume(RabbitConstants.DISTRIBUTING_QUEUE, true, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void distributeMessage(String consumerTag, Delivery delivery) throws IOException {
        String routingKey = delivery.getEnvelope().getRoutingKey();
        String destinationKey = routingKey.substring(routingKey.lastIndexOf('.') + 1);

        logger.info(String.format("Distributing message with routing key '%s'", destinationKey));

        channel.basicPublish(RabbitConstants.PICKUP_EXCHANGE, destinationKey, null, delivery.getBody());
    }
}
