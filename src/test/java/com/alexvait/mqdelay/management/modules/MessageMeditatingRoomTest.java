package com.alexvait.mqdelay.management.modules;

import com.alexvait.mqdelay.management.helpers.NamingUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.alexvait.mqdelay.management.message.DelayedMessage;
import com.rabbitmq.client.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@DisplayName("Test the initialisation of the waiting hall")
class MessageMeditatingRoomTest {

    @Mock
    private Channel channel;

    @Test
    void testConstructionWithNullChannel() {
        assertThrows(NullPointerException.class, () -> new MessageMeditatingRoom(null, 1));
    }

    @Test
    void testConstructionWithZeroMaxDelay() {
        assertThrows(IllegalArgumentException.class, () -> new MessageMeditatingRoom(channel, 0));
    }

    @Test
    void testConstructionWithNegativeMaxDelay() {
        assertThrows(IllegalArgumentException.class, () -> new MessageMeditatingRoom(channel, -1));
    }

    @Nested
    @DisplayName("Test MessageMeditatingRoom methods")
    class MessageMeditatingRoomMethodsTest {
        private final int maxLevel = new Random(System.currentTimeMillis()).nextInt(20);

        @Mock
        private Channel channel;

        private String randomRoutingKey;
        private String randomMessage;
        private DelayedMessage delayedMessage;

        private MessageMeditatingRoom messageMeditatingRoom;

        @BeforeEach
        public void setUp() throws NoSuchMethodException {
            MockitoAnnotations.initMocks(this);
            messageMeditatingRoom = new MessageMeditatingRoom(channel, maxLevel);
            randomRoutingKey = RandomStringUtils.randomAlphabetic(20);
            randomMessage = RandomStringUtils.randomAlphabetic(300);

            delayedMessage = new DelayedMessage(0, randomRoutingKey, randomMessage);
        }

        @Test
        @DisplayName("Test the call to the basicConsume on the channel")
        public void testRun() throws IOException {
            messageMeditatingRoom.run();
            verify(channel).basicConsume(eq(RabbitConstants.PREPARING_QUEUE), eq(true), any(DeliverCallback.class), any(CancelCallback.class));
        }

        @Test
        @DisplayName("Test that 'requeueOrDistribute' method calls 'basicPublish' and requeues")
        void testRequeueOrDistributeRequeue() throws InvocationTargetException, IllegalAccessException, IOException {
            // set the delay time higher than the maximal delay in the queues
            long randomIncrease = new Random(System.currentTimeMillis()).nextInt(10);
            long delay = (long) Math.pow(2, maxLevel) + randomIncrease;

            delayedMessage.setDelayTime(delay);

            messageMeditatingRoom.requeueOrDistribute(null,
                    new Delivery(
                            new Envelope(0L, true, null, randomRoutingKey),
                            null, delayedMessage.toJsonString().getBytes()
                    )
            );

            // the maximal waiting time was subtracted from the delay time, so only the additional time remains
            delayedMessage.setDelayTime(randomIncrease);

            verify(channel).basicPublish(RabbitConstants.WAITING_HALL_EXCHANGE, "",
                    null, delayedMessage.toJsonString().getBytes());
        }

        @Test
        @DisplayName("Test that 'requeueOrDistribute' method calls 'basicPublish' and distributes")
        void testRequeueOrDistributeDistribute() throws InvocationTargetException, IllegalAccessException, IOException {
            // set the delay time lower than the maximal delay in the queues
            long maxDelay = (long) Math.pow(2, maxLevel);
            long delay = maxDelay / 2;

            delayedMessage.setDelayTime(delay);

            messageMeditatingRoom.requeueOrDistribute(null,
                    new Delivery(
                            new Envelope(0L, true, null, randomRoutingKey),
                            null, delayedMessage.toJsonString().getBytes()
                    )
            );

            NamingUtil namingUtil = new NamingUtil(maxDelay);
            String receivingExchange = namingUtil.getExchangeNameForTtl(maxDelay);
            String topic = namingUtil.createTopicFromTtl(delay) + "." + randomRoutingKey;

            verify(channel).basicPublish(receivingExchange, topic,
                    null, randomMessage.getBytes());
        }
    }
}