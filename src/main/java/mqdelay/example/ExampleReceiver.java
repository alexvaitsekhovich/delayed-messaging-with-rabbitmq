package mqdelay.example;

import com.alexvait.mqdelay.management.helpers.rabbit.ConnectionFactoryUtil;
import com.alexvait.mqdelay.management.helpers.rabbit.RabbitConstants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Example for receiving queue. Specify the queue name and topic in the command line.
 */
public class ExampleReceiver {
    private static final Logger logger = LoggerFactory.getLogger(ExampleReceiver.class);

    public static void main(String[] argv) throws Exception {

        if (argv.length != 2) {
            logger.error("Usage: java DelayedMessageReceiver <receiving queue> <routing key>");
            return;
        }

        String receivingQueue = argv[0];
        String routingKey = argv[1];

        try (Channel channel = new ConnectionFactoryUtil().getChannel()) {
            channel.queueDeclare(receivingQueue, false, false, false, null);
            channel.queueBind(receivingQueue, RabbitConstants.PICKUP_EXCHANGE, routingKey);

            logger.info("Receiving service is ready");

            DeliverCallback deliverCallback = ExampleReceiver::handle;

            channel.basicConsume(receivingQueue, true, deliverCallback, consumerTag -> {
            });
        }
    }

    private static void handle(String consumerTag, Delivery delivery) {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        logger.info("Service received message : " + message);
    }
}
