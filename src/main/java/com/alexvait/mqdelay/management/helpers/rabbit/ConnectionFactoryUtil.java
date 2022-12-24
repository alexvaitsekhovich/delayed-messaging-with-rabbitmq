package com.alexvait.mqdelay.management.helpers.rabbit;

import com.alexvait.mqdelay.management.helpers.PropertiesUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants.*;

/**
 * Helper utility for creating a RabbitMQ connection factory.
 */
public final class ConnectionFactoryUtil {
    private final PropertiesUtil propertiesUtil;

    public ConnectionFactoryUtil() {
        this(new PropertiesUtil());
    }

    public ConnectionFactoryUtil(PropertiesUtil propUtil) {
        this.propertiesUtil = Objects.requireNonNull(propUtil, "PropertiesUtil cannot be null");
    }

    public Channel getChannel() throws IOException, TimeoutException {
        var connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(propertiesUtil.getProperty(RABBIT_MQ_HOST));
        connectionFactory.setPort(Integer.parseInt(propertiesUtil.getProperty(RABBIT_MQ_PORT)));
        connectionFactory.setVirtualHost(propertiesUtil.getProperty(RABBIT_MQ_VIRTUAL_HOST));
        connectionFactory.setUsername(propertiesUtil.getProperty(RABBIT_MQ_USER));
        connectionFactory.setPassword(propertiesUtil.getProperty(RABBIT_MQ_PASSWORD));

        return connectionFactory.newConnection().createChannel();
    }
}
