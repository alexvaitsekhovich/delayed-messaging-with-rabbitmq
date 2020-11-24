package com.alexvait.mqdelay.management;

import com.alexvait.mqdelay.management.helpers.rabbit.ConnectionFactoryUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.MQueueUtil;
import com.alexvait.mqdelay.management.modules.MessageDistributor;
import com.alexvait.mqdelay.management.modules.MessageMeditatingRoom;
import com.alexvait.mqdelay.management.modules.MqStructureInitializer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Initialise and start the required modules.
 */
public final class MqDelayStarter {
    private static final Logger logger = LoggerFactory.getLogger(MqDelayStarter.class);
    private final int levels;

    public MqDelayStarter(int levels) {
        this.levels = levels - 1;
    }

    public void start() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactoryUtil().getFactory();
        Channel channel = factory.newConnection().createChannel();

        new MqStructureInitializer(new MQueueUtil(channel), levels).init();

        new Thread(new MessageMeditatingRoom(channel, levels)).start();

        new Thread(new MessageDistributor(channel)).start();

        logger.info("Delayed messaging system started");
    }
}
