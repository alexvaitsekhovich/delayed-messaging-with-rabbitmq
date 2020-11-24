package com.alexvait.mqdelay.functional_testing;

import com.alexvait.mqdelay.management.helpers.rabbit.ConnectionFactoryUtil;
import com.alexvait.mqdelay.management.sender.DelayedMessageSender;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Message sender for functional tests.
 */
public class FunctionalTestingSender {
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactoryUtil().getFactory();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            send(channel, 3);
            send(channel, 5);
            send(channel, 11);
            send(channel, 19);
            send(channel, 26);
        }
    }

    private static void send(Channel channel, long delay) throws IOException {
        LocalDateTime sendTime = LocalDateTime.now();
        DelayedMessageSender.send(channel, delay, "functional-testing", delay + "#" + sendTime);
    }

}
