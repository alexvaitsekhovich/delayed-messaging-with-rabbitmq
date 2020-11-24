package com.alexvait.mqdelay.management.helpers.rabbit;

/**
 * Container class for constants.
 */
public final class RabbitConstants {
    public static final String RABBIT_MQ_HOST = "rabbitMqHost";
    public static final String RABBIT_MQ_PORT = "rabbitMqPort";
    public static final String RABBIT_MQ_VIRTUAL_HOST = "rabbitMqVirtualHost";
    public static final String RABBIT_MQ_USER = "rabbitMqUser";
    public static final String RABBIT_MQ_PASSWORD = "rabbitMqPassword";

    public static final int MILLISECONDS = 1000;

    public static final String DISTRIBUTING_QUEUE = "distributing_queue";

    public static final String PICKUP_EXCHANGE = "PICKUP_EXCHANGE";

    public static final String CHECKIN_EXCHANGE = "CHECKIN_EXCHANGE";
    public static final String PREPARING_QUEUE = "preparing_queue";

    public static final String WAITING_HALL_EXCHANGE = "WAITING_HALL_EXCHANGE";
    public static final String WAITING_HALL_QUEUE = "waiting_hall_queue";

    private RabbitConstants() {
    }
}
