package com.alexvait.mqdelay.management.helpers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Test creation of names and routing keys")
class NamingUtilTest {
    private static NamingUtil namingUtil;

    @BeforeAll
    static public void init() {
        namingUtil = new NamingUtil(16);
    }

    @ParameterizedTest(name = "#{index}, arguments: {0},{1},{2}")
    @MethodSource("argsProviderForExchangeNameForTtl")
    @DisplayName("Test created exchange name")
    public void testGetExchangeNameForTtl(int levels, long ttl, String expectedExchangeName) {
        NamingUtil namingUtil = new NamingUtil(levels);
        String exchangeName = namingUtil.getExchangeNameForTtl(ttl);
        assertEquals(expectedExchangeName, exchangeName);
    }

    @ParameterizedTest(name = "#{index}, arguments: {0},{1},{2}")
    @MethodSource("argsProviderForTopicFromTtlDelay")
    @DisplayName("Test created routing key for delayed queue")
    public void testGetTopicFromTtlDelay(int levels, long ttl, String expectedTopic) {
        NamingUtil namingUtil = new NamingUtil(levels);
        String topic = namingUtil.getTopicFromTtlDelaying(ttl);
        assertEquals(expectedTopic, topic);
    }

    @ParameterizedTest(name = "#{index}, arguments: {0},{1},{2}")
    @MethodSource("argsProviderForTopicFromTtlPass")
    @DisplayName("Test created routing key for passing queue")
    public void testGetTopicFromTtlPassing(int levels, long ttl, String expectedTopic) {
        NamingUtil namingUtil = new NamingUtil(levels);
        String topic = namingUtil.getTopicFromTtlPassing(ttl);
        assertEquals(expectedTopic, topic);
    }

    @ParameterizedTest(name = "#{index}, arguments: {0},{1},{2}")
    @MethodSource("argsProviderForDelayQueueNameFromTtl")
    @DisplayName("Test created name for delayed queue")
    public void testGetDelayQueueNameForTtl(int levels, long ttl, String expectedQueueName) {
        NamingUtil namingUtil = new NamingUtil(levels);
        String queueName = namingUtil.getDelayQueueNameForTtl(ttl);
        assertEquals(expectedQueueName, queueName);
    }

    @ParameterizedTest(name = "#{index}, arguments: {0},{1},{2}")
    @MethodSource("argsProviderForPassingQueueNameFromTtl")
    @DisplayName("Test created name for passing queue")
    public void testGetPassingQueueNameForTtl(int levels, long ttl, String expectedQueueName) {
        NamingUtil namingUtil = new NamingUtil(levels);
        String queueName = namingUtil.getPassingQueueNameForTtl(ttl);
        assertEquals(expectedQueueName, queueName);
    }

    private static Stream<Arguments> argsProviderForExchangeNameForTtl() {
        return Stream.of(
                arguments(2, 1, "EXCHANGE_LEVEL_01"),
                arguments(2, 2, "EXCHANGE_LEVEL_10"),
                arguments(2, 128, "EXCHANGE_LEVEL_10000000"),
                arguments(4, 1, "EXCHANGE_LEVEL_001"),
                arguments(4, 2, "EXCHANGE_LEVEL_010"),
                arguments(4, 4, "EXCHANGE_LEVEL_100"),
                arguments(8, 1, "EXCHANGE_LEVEL_0001"),
                arguments(8, 8, "EXCHANGE_LEVEL_1000"),
                arguments(16, 1, "EXCHANGE_LEVEL_00001"),
                arguments(16, 2, "EXCHANGE_LEVEL_00010"),
                arguments(16, 4, "EXCHANGE_LEVEL_00100"),
                arguments(16, 8, "EXCHANGE_LEVEL_01000"),
                arguments(16, 16, "EXCHANGE_LEVEL_10000"),
                arguments(32, 16, "EXCHANGE_LEVEL_010000"),
                arguments(32, 32, "EXCHANGE_LEVEL_100000")
        );
    }

    private static Stream<Arguments> argsProviderForTopicFromTtlDelay() {
        return Stream.of(
                arguments(2, 1, "*.1.*"),
                arguments(2, 2, "1.*.*"),
                arguments(2, 128, "1.*.*.*.*.*.*.*.*"),
                arguments(4, 1, "*.*.1.*"),
                arguments(4, 2, "*.1.*.*"),
                arguments(4, 4, "1.*.*.*"),
                arguments(8, 1, "*.*.*.1.*"),
                arguments(8, 8, "1.*.*.*.*"),
                arguments(16, 1, "*.*.*.*.1.*"),
                arguments(16, 2, "*.*.*.1.*.*"),
                arguments(16, 4, "*.*.1.*.*.*"),
                arguments(16, 8, "*.1.*.*.*.*"),
                arguments(16, 16, "1.*.*.*.*.*"),
                arguments(32, 16, "*.1.*.*.*.*.*"),
                arguments(32, 32, "1.*.*.*.*.*.*")
        );
    }

    private static Stream<Arguments> argsProviderForTopicFromTtlPass() {
        return Stream.of(
                arguments(2, 1, "*.0.*"),
                arguments(2, 2, "0.*.*"),
                arguments(2, 128, "0.*.*.*.*.*.*.*.*"),
                arguments(4, 1, "*.*.0.*"),
                arguments(4, 2, "*.0.*.*"),
                arguments(4, 4, "0.*.*.*"),
                arguments(8, 1, "*.*.*.0.*"),
                arguments(8, 8, "0.*.*.*.*"),
                arguments(16, 1, "*.*.*.*.0.*"),
                arguments(16, 2, "*.*.*.0.*.*"),
                arguments(16, 4, "*.*.0.*.*.*"),
                arguments(16, 8, "*.0.*.*.*.*"),
                arguments(16, 16, "0.*.*.*.*.*"),
                arguments(32, 16, "*.0.*.*.*.*.*"),
                arguments(32, 32, "0.*.*.*.*.*.*")
        );
    }

    private static Stream<Arguments> argsProviderForDelayQueueNameFromTtl() {
        return Stream.of(
                arguments(2, 1, "delay_queue_01"),
                arguments(4, 1, "delay_queue_001"),
                arguments(4, 2, "delay_queue_010"),
                arguments(4, 4, "delay_queue_100"),
                arguments(8, 4, "delay_queue_0100"),
                arguments(16, 1, "delay_queue_00001"),
                arguments(16, 2, "delay_queue_00010"),
                arguments(16, 4, "delay_queue_00100"),
                arguments(16, 8, "delay_queue_01000"),
                arguments(16, 16, "delay_queue_10000")
        );
    }

    private static Stream<Arguments> argsProviderForPassingQueueNameFromTtl() {
        return Stream.of(
                arguments(2, 1, "pass_queue_01"),
                arguments(4, 1, "pass_queue_001"),
                arguments(4, 2, "pass_queue_010"),
                arguments(4, 4, "pass_queue_100"),
                arguments(8, 4, "pass_queue_0100"),
                arguments(16, 1, "pass_queue_00001"),
                arguments(16, 2, "pass_queue_00010"),
                arguments(16, 4, "pass_queue_00100"),
                arguments(16, 8, "pass_queue_01000"),
                arguments(16, 16, "pass_queue_10000")
        );
    }
}