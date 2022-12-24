package com.alexvait.mqdelay.management.sender;

import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.alexvait.mqdelay.management.message.DelayedMessage;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Sender for the delayed messages.
 */
public final class DelayedMessageSender {
    private DelayedMessageSender() {
    }

    public static void send(Channel channel, long delay, String routingKey, String message) throws IOException {
        var delayedMessage = new DelayedMessage(delay, routingKey, message);
        channel.basicPublish(RabbitConstants.CHECKIN_EXCHANGE, "", null,
                delayedMessage.toJsonString().getBytes(StandardCharsets.UTF_8));
    }
}
