package com.alexvait.mqdelay.management.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test the delayed message object")
class DelayedMessageTest {

    @Test
    @DisplayName("Test deserialization from a json string")
    void testFromJsonString() throws JsonProcessingException {
        int delayTime = new Random(System.currentTimeMillis()).nextInt(1000);
        String routingKey = RandomStringUtils.randomAlphabetic(50);
        String payload = RandomStringUtils.randomAlphabetic(300);

        String jsonMessage = String.format("{\"delayTime\":%d,\"routingKey\":\"%s\",\"payload\":\"%s\"}",
                delayTime, routingKey, payload);
        DelayedMessage message = DelayedMessage.fromJsonString(jsonMessage);

        assertEquals(delayTime, message.getDelayTime());
        assertEquals(routingKey, message.getRoutingKey());
        assertEquals(payload, message.getPayload());
    }

    @Test
    @DisplayName("Test serialization to json string")
    void testToJsonString() throws JsonProcessingException {
        int delayTime = new Random(System.currentTimeMillis()).nextInt(1000);
        String routingKey = RandomStringUtils.randomAlphabetic(50);
        String payload = RandomStringUtils.randomAlphabetic(300);

        DelayedMessage message = new DelayedMessage(delayTime, routingKey, payload);
        String expectedMessage = String.format("{\"delayTime\":%d,\"routingKey\":\"%s\",\"payload\":\"%s\"}",
                delayTime, routingKey, payload);

        assertEquals(expectedMessage, message.toJsonString());
    }

    @Test
    @DisplayName("Test toString()")
    void testToString() {
        int delayTime = new Random(System.currentTimeMillis()).nextInt(1000);
        String routingKey = RandomStringUtils.randomAlphabetic(50);
        String payload = RandomStringUtils.randomAlphabetic(300);

        DelayedMessage message = new DelayedMessage(delayTime, routingKey, payload);
        String expectedString = String.format("MqMessage {deliveryTime='%d', routingKey='%s', payload='%s'}",
                delayTime, routingKey, payload);

        assertEquals(expectedString, message.toString());
    }

    @Test
    @DisplayName("Test negative delay time throws exception")
    void testNegativeDelayTime() {
        assertThrows(IllegalArgumentException.class, () -> new DelayedMessage(-1, "key", "msg"));
    }

    @Test
    @DisplayName("Test null routing key throws exception")
    void testNullRoutingKey() {
        assertThrows(NullPointerException.class, () -> new DelayedMessage(1, null, "msg"));
    }

    @Test
    @DisplayName("Test zero length routing key throws exception")
    void testEmptyRoutingKey() {
        assertThrows(NullPointerException.class, () -> new DelayedMessage(1, "", "msg"));
    }

    @Test
    @DisplayName("Test null message throws exception")
    void testNullMessage() {
        assertThrows(NullPointerException.class, () -> new DelayedMessage(1, "key", null));
    }

    @Test
    @DisplayName("Test zero length message throws exception")
    void testEmptyMessage() {
        assertThrows(NullPointerException.class, () -> new DelayedMessage(1, "key", ""));
    }
}