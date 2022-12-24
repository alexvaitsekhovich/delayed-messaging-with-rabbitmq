package com.alexvait.mqdelay.functional_testing;

import com.alexvait.mqdelay.management.helpers.rabbit.ConnectionFactoryUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Message receiver for functional tests.
 */
public class FunctionalTestingReceiver {
    public static final double ERROR_FRACTION = 200.0;
    private static final Logger logger = LoggerFactory.getLogger(FunctionalTestingReceiver.class);

    public static void main(String[] argv) throws Exception {

        String receivingQueue = "testing-queue";
        String routingKey = "functional-testing";

        try (Channel channel = new ConnectionFactoryUtil().getChannel()) {
            channel.queueDeclare(receivingQueue, false, false, false, null);
            channel.queueBind(receivingQueue, RabbitConstants.PICKUP_EXCHANGE, routingKey);

            logger.info("Functional testing receiving service is ready");

            DeliverCallback deliverCallback = FunctionalTestingReceiver::handle;

            channel.basicConsume(receivingQueue, true, deliverCallback, consumerTag -> {
            });
        }
    }

    private static void handle(String consumerTag, Delivery delivery) {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        String[] messageParts = message.split("#");

        long requestedDelay = Long.parseLong(messageParts[0]);
        LocalDateTime sendDateTime = LocalDateTime.parse(messageParts[1]);

        long delayTime = ChronoUnit.SECONDS.between(sendDateTime, LocalDateTime.now());

        long errorMargin = Math.round(requestedDelay / ERROR_FRACTION);

        if (Math.abs(requestedDelay - delayTime) > errorMargin) {
            logger.error(String.format("Error: %d vs %d", requestedDelay, delayTime));
            System.exit(1);
        } else {
            logger.info(String.format("Success: %d vs %d", requestedDelay, delayTime));
        }

    }
}
