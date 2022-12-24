package com.alexvait.mqdelay.management.modules;

import com.alexvait.mqdelay.management.helpers.NamingUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.MQueueUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;

import java.io.IOException;
import java.util.Objects;

/**
 * Setting up the system of exchanges and queues, based on the provided maximal binary level.
 */
public class MqStructureInitializer {

    private final long maxDelay;
    private final NamingUtil namingUtil;
    private final MQueueUtil queueCreator;

    public MqStructureInitializer(MQueueUtil queueCreator, int levels) {
        if (levels <= 0)
            throw new IllegalArgumentException("levels number must be a positive number");

        this.queueCreator = Objects.requireNonNull(queueCreator, "Queue creator cannot be null");
        maxDelay = (long) Math.pow(2, levels);
        namingUtil = new NamingUtil(maxDelay);
    }

    public void init() throws IOException {
        createCheckinQueueAndExchange();
        createDistributionQueueAndExchange();

        queueCreator.createDirectExchange(RabbitConstants.PICKUP_EXCHANGE);

        for (long ttl = 1; ttl <= maxDelay; ttl *= 2) {
            String exchangeName = namingUtil.getExchangeNameForTtl(ttl);

            queueCreator.createTopicExchange(exchangeName);
            createQueuePair(ttl, exchangeName);
        }
    }

    private void createQueuePair(long ttl, String exchangeName) throws IOException {
        String deadLetterExchange = namingUtil.getExchangeNameForTtl(ttl / 2);
        queueCreator.createAndBind(namingUtil.getDelayQueueNameForTtl(ttl), exchangeName, deadLetterExchange,
                ttl * RabbitConstants.MILLISECONDS, namingUtil.getTopicFromTtlDelaying(ttl));

        queueCreator.createAndBind(namingUtil.getPassingQueueNameForTtl(ttl), exchangeName, deadLetterExchange,
                1, namingUtil.getTopicFromTtlPassing(ttl));
    }

    private void createDistributionQueueAndExchange() throws IOException {
        String finalExchange = namingUtil.getExchangeNameForTtl(0);
        queueCreator.createTopicExchange(finalExchange);
        queueCreator.createAndBind(RabbitConstants.DISTRIBUTING_QUEUE, finalExchange, "#.*");
    }

    private void createCheckinQueueAndExchange() throws IOException {
        queueCreator.createDirectExchange(RabbitConstants.CHECKIN_EXCHANGE);
        queueCreator.createDirectExchange(RabbitConstants.WAITING_HALL_EXCHANGE);

        queueCreator.createAndBind(RabbitConstants.PREPARING_QUEUE, RabbitConstants.CHECKIN_EXCHANGE, "");
        queueCreator.createAndBind(RabbitConstants.WAITING_HALL_QUEUE, RabbitConstants.WAITING_HALL_EXCHANGE,
                RabbitConstants.CHECKIN_EXCHANGE, maxDelay * RabbitConstants.MILLISECONDS, "");
    }
}