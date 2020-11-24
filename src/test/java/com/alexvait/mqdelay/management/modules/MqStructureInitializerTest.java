package com.alexvait.mqdelay.management.modules;

import com.alexvait.mqdelay.management.helpers.rabbit.MQueueUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;


@DisplayName("Test the MqStructureInitializer")
class MqStructureInitializerTest {

    @Mock
    private MQueueUtil queueCreator;

    @Test
    void testConstructionWithNullQueueCreator() {
        assertThrows(NullPointerException.class, () -> new MqStructureInitializer(null, 1));
    }

    @Test
    void testConstructionWithZeroLevels() {
        assertThrows(IllegalArgumentException.class, () -> new MqStructureInitializer(queueCreator, 0));
    }

    @Test
    void testConstructionWithNegativeLevels() {
        assertThrows(IllegalArgumentException.class, () -> new MqStructureInitializer(queueCreator, -1));
    }

    @Nested
    @DisplayName("Test the initialisation of the whole system")
    class MqStructureInitializerTestWhole {

        private final int LEVELS = 3;

        @Mock
        private MQueueUtil queueCreator;

        private MqStructureInitializer structureInitializer;

        @BeforeEach
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            structureInitializer = new MqStructureInitializer(queueCreator, LEVELS);
        }

        @Test
        @DisplayName("Test that all exchanges and queues are set up correctly")
        public void testSystemInit() throws IOException {
            structureInitializer.init();
            long maxDelay = (long) Math.pow(2, LEVELS) * 1000;

            String finalExchange = "EXCHANGE_LEVEL_0000";

            assertAll(
                    () -> verify(queueCreator).createTopicExchange(finalExchange),
                    () -> verify(queueCreator).createDirectExchange(RabbitConstants.PICKUP_EXCHANGE),
                    () -> verify(queueCreator).createDirectExchange(RabbitConstants.CHECKIN_EXCHANGE),
                    () -> verify(queueCreator).createDirectExchange(RabbitConstants.WAITING_HALL_EXCHANGE),

                    () -> verify(queueCreator).createAndBind(RabbitConstants.DISTRIBUTING_QUEUE, finalExchange, "#.*"),
                    () -> verify(queueCreator).createAndBind(RabbitConstants.PREPARING_QUEUE, RabbitConstants.CHECKIN_EXCHANGE, ""),
                    () -> verify(queueCreator).createAndBind(RabbitConstants.WAITING_HALL_QUEUE, RabbitConstants.WAITING_HALL_EXCHANGE,
                            RabbitConstants.CHECKIN_EXCHANGE, maxDelay, ""),

                    () -> verify(queueCreator).createTopicExchange("EXCHANGE_LEVEL_0001"),
                    () -> verify(queueCreator).createTopicExchange("EXCHANGE_LEVEL_0010"),
                    () -> verify(queueCreator).createTopicExchange("EXCHANGE_LEVEL_0100"),
                    () -> verify(queueCreator).createTopicExchange("EXCHANGE_LEVEL_1000"),

                    () -> verify(queueCreator).createAndBind("delay_queue_0001", "EXCHANGE_LEVEL_0001",
                            "EXCHANGE_LEVEL_0000", 1000, "*.*.*.1.*"),
                    () -> verify(queueCreator).createAndBind("pass_queue_0001", "EXCHANGE_LEVEL_0001",
                            "EXCHANGE_LEVEL_0000", 1, "*.*.*.0.*"),

                    () -> verify(queueCreator).createAndBind("delay_queue_0010", "EXCHANGE_LEVEL_0010",
                            "EXCHANGE_LEVEL_0001", 2000, "*.*.1.*.*"),
                    () -> verify(queueCreator).createAndBind("pass_queue_0010", "EXCHANGE_LEVEL_0010",
                            "EXCHANGE_LEVEL_0001", 1, "*.*.0.*.*"),

                    () -> verify(queueCreator).createAndBind("delay_queue_0100", "EXCHANGE_LEVEL_0100",
                            "EXCHANGE_LEVEL_0010", 4000, "*.1.*.*.*"),
                    () -> verify(queueCreator).createAndBind("pass_queue_0100", "EXCHANGE_LEVEL_0100",
                            "EXCHANGE_LEVEL_0010", 1, "*.0.*.*.*"),

                    () -> verify(queueCreator).createAndBind("delay_queue_1000", "EXCHANGE_LEVEL_1000",
                            "EXCHANGE_LEVEL_0100", 8000, "1.*.*.*.*"),
                    () -> verify(queueCreator).createAndBind("pass_queue_1000", "EXCHANGE_LEVEL_1000",
                            "EXCHANGE_LEVEL_0100", 1, "0.*.*.*.*")
            );
        }
    }
}