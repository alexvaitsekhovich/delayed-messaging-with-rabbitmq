package com.alexvait.mqdelay.management.helpers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Test the properties reader")
class PropertiesUtilTest {

    @Test
    @DisplayName("Test overwriting the properties from the config file with system properties")
    void testGetProperty() throws IllegalAccessException, NoSuchFieldException {
        String propertyName = "rabbitMqHost";
        String expectedPropertyVal = RandomStringUtils.randomAlphabetic(300);
        System.setProperty(propertyName, expectedPropertyVal);

        // clean data retrieved during previous tests, start from clean singleton
        Field instance = PropertiesUtil.class.getDeclaredField("properties");
        instance.setAccessible(true);
        instance.set(null, null);

        String savedPropertyVal = new PropertiesUtil().getProperty(propertyName);

        assertEquals(expectedPropertyVal, savedPropertyVal);
    }

    @Test
    @DisplayName("Test returning property null for null key")
    void testGetPropertyNullForNullKey() {
        assertNull(new PropertiesUtil().getProperty(null));
    }

    @Test
    @DisplayName("Test returning property null for an empty key string")
    void testGetPropertyNullForEmptyKey() {
        assertNull(new PropertiesUtil().getProperty(""));
    }

}