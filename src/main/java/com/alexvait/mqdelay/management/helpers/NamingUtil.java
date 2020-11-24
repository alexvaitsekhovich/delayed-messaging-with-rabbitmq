package com.alexvait.mqdelay.management.helpers;

/**
 * Helper utility for creating names for queues and exchanges based on the specified TTL.
 * Builds a queue name, exchange name or a tipic consisting of ones and zeroes, representing the delay in binary format
 * where 1 is for delaying at the specified time, 0 is for passing through
 */
public final class NamingUtil {
    private final long maxDelay;

    public NamingUtil(long maxDelay) {
        this.maxDelay = maxDelay;
    }

    public String getTopicFromTtlDelaying(long ttl) {
        return getTopicFromTtl(ttl, true);
    }

    public String getTopicFromTtlPassing(long ttl) {
        return getTopicFromTtl(ttl, false);
    }

    private String getTopicFromTtl(long ttl, boolean delay) {
        String topic = createTopicFromTtl(ttl);
        String delayMqTopic = topic.replace("0", "*") + ".*";

        return delay ? delayMqTopic : delayMqTopic.replace("1", "0");
    }

    public String getExchangeNameForTtl(long ttl) {
        return getElementNameFromTtl("EXCHANGE_LEVEL_", ttl);
    }

    public String getDelayQueueNameForTtl(long ttl) {
        return getElementNameFromTtl("delay_queue_", ttl);
    }

    public String getPassingQueueNameForTtl(long ttl) {
        return getElementNameFromTtl("pass_queue_", ttl);
    }

    private String getElementNameFromTtl(String name, long ttl) {
        return name + createBinaryStringFromTtl(ttl);
    }

    private String createBinaryStringFromTtl(long ttl) {
        int bStringLength = Long.toBinaryString(maxDelay).length();
        return String.format("%" + bStringLength + "s", Long.toBinaryString(ttl)).replace(' ', '0');
    }

    public String createTopicFromTtl(long ttl) {
        String binaryString = createBinaryStringFromTtl(ttl);
        return String.join(".", binaryString.split(""));
    }
}
