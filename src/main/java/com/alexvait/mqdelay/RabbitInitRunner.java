package com.alexvait.mqdelay;

import com.alexvait.mqdelay.management.MqDelayStarter;

public final class RabbitInitRunner {
    public static void main(String[] argv) throws Exception {
        int levels = 4;
        new MqDelayStarter(levels).start();
    }

}
