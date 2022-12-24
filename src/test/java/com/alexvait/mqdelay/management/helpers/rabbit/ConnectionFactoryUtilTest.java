package com.alexvait.mqdelay.management.helpers.rabbit;

import com.alexvait.mqdelay.management.helpers.PropertiesUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.UnknownHostException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Test the utility for creation of connection factory")
@ExtendWith(MockitoExtension.class)
class ConnectionFactoryUtilTest {

    @Mock
    private PropertiesUtil propertiesUtil;

    @Test
    @DisplayName("Test setting of configuration data on the connection factory")
    void testGetFactory() {
        doReturn(RandomStringUtils.randomAlphabetic(300)).when(propertiesUtil).getProperty(RabbitConstants.RABBIT_MQ_HOST);
        doReturn(String.valueOf(new Random().nextInt(10000))).when(propertiesUtil).getProperty(RabbitConstants.RABBIT_MQ_PORT);

        assertThrows(UnknownHostException.class, () -> new ConnectionFactoryUtil(propertiesUtil).getChannel());
        verify(propertiesUtil, times(5)).getProperty(anyString());
    }
}