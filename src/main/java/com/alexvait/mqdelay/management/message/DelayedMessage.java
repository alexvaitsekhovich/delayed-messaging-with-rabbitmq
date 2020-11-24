package com.alexvait.mqdelay.management.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Message container, specifying the delay time, routing key for the destination queue and
 * the message that shall be delivered with the specified delay.
 */
public class DelayedMessage {
    private long delayTime;
    private String routingKey;
    private String payload;

    // required for Jackson deserialization
    public DelayedMessage() {
    }

    public DelayedMessage(long delayTime, String routingKey, String payload) {

        if (delayTime < 0)
            throw new IllegalArgumentException(String.format("Delay time cannot negative. Parameter was: %d", delayTime));

        if (routingKey == null || routingKey.length()==0)
            throw new NullPointerException(String.format("Routing key cannot be null or empty. Parameter was: %s", routingKey));

        if (payload == null || payload.length()==0)
            throw new NullPointerException(String.format("Payload cannot be null or empty. Parameter was: %s", payload));

        this.delayTime = delayTime;
        this.routingKey = routingKey;
        this.payload = payload;
    }

    public static DelayedMessage fromJsonString(String jsonMessage) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonMessage, DelayedMessage.class);
    }

    public String toJsonString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "MqMessage {"
                + "deliveryTime='" + delayTime + '\''
                + ", routingKey='" + routingKey + '\''
                + ", payload='" + payload + '\''
                + '}';
    }
}
