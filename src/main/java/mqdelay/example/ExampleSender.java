package mqdelay.example;

import com.alexvait.mqdelay.management.helpers.rabbit.ConnectionFactoryUtil;
import com.alexvait.mqdelay.management.sender.DelayedMessageSender;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message sender for functional tests.
 */
public class ExampleSender {
    private static final Logger logger = LoggerFactory.getLogger(ExampleSender.class);

    public static void main(String[] argv) throws Exception {
        if (argv.length != 3) {
            logger.error("Usage: java ExampleSender <routing key> <delay in seconds> <message>");
            return;
        }

        String routingKey = argv[0];
        long delay = Long.parseLong(argv[1]);
        String message = argv[2];

        try (Channel channel = new ConnectionFactoryUtil().getChannel()) {
            DelayedMessageSender.send(channel, delay, routingKey, message);
        }
    }
}
