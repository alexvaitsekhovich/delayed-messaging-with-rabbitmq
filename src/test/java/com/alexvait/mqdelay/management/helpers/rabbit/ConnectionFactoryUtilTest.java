package com.alexvait.mqdelay.management.helpers.rabbit;

import com.alexvait.mqdelay.management.helpers.PropertiesUtil;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@DisplayName("Test the utility for creation of connection factory")
class ConnectionFactoryUtilTest {

    @Mock
    private PropertiesUtil propertiesUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Test setting of configuration data on the connection factory")
    void testGetFactory() {
        String randomHost = RandomStringUtils.randomAlphabetic(300);
        doReturn(randomHost).when(propertiesUtil).getProperty(RabbitConstants.RABBIT_MQ_HOST);
        doReturn(String.valueOf(new Random().nextInt(10000))).when(propertiesUtil).getProperty(RabbitConstants.RABBIT_MQ_PORT);

        ConnectionFactory factory = new ConnectionFactoryUtil(propertiesUtil).getFactory();

        assertEquals(randomHost, factory.getHost());
    }
}