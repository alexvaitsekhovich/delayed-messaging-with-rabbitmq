package com.alexvait.mqdelay.management.modules;

import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.rabbitmq.client.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@DisplayName("Test the message distributor class")
class MessageDistributorTest {
    @Mock
    private Channel channel;

    private MessageDistributor distributor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        distributor = new MessageDistributor(channel);
    }

    @Test
    @DisplayName("Test that running the thread calls 'basicConsume' on the channel")
    void testRunMethod() throws IOException {
        distributor.run();
        verify(channel).basicConsume(eq(RabbitConstants.DISTRIBUTING_QUEUE),
                eq(true), any(DeliverCallback.class), any(CancelCallback.class));
    }

    @Test
    @DisplayName("Test that 'distributeMessage' method calls 'basicPublish' on the channel")
    void testDistributeMessage() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        // get access to the private method
        Method distributeMessageMethod = MessageDistributor.class.getDeclaredMethod("distributeMessage", String.class, Delivery.class);
        distributeMessageMethod.setAccessible(true);

        String randomRoutingKey = RandomStringUtils.randomAlphabetic(50);
        String randomMessage = RandomStringUtils.randomAlphabetic(50);

        distributeMessageMethod.invoke(distributor, null,
                new Delivery(
                        new Envelope(0L, true, null, randomRoutingKey),
                        null, randomMessage.getBytes()
                )
        );

        verify(channel).basicPublish(RabbitConstants.PICKUP_EXCHANGE, randomRoutingKey,
                null, randomMessage.getBytes());
    }
}