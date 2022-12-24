package com.alexvait.mqdelay.management.sender;

import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.alexvait.mqdelay.management.message.DelayedMessage;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.mockito.Mockito.verify;

@DisplayName("Test the sender of delayed messages")
@ExtendWith(MockitoExtension.class)
class DelayedMessageSenderTest {
    @Mock
    private Channel channel;

    @Test
    @DisplayName("Test that the message is send correctly to the channel")
    public void testSend() throws IOException {
        int randomDelay = new Random(System.currentTimeMillis()).nextInt(100_000);

        String randomRoutingKey = RandomStringUtils.randomAlphabetic(20);
        String randomMessage = RandomStringUtils.randomAlphabetic(300);

        DelayedMessageSender.send(channel, randomDelay, randomRoutingKey, randomMessage);
        verify(channel).basicPublish(RabbitConstants.CHECKIN_EXCHANGE, "", null,
                new DelayedMessage(randomDelay, randomRoutingKey, randomMessage).
                        toJsonString().getBytes(StandardCharsets.UTF_8));

    }
}