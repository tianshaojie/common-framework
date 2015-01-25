package io.github.jsbd.common.lang.transport;

import java.util.concurrent.TimeUnit;

public interface SenderSync {

    Object sendAndWait(Object bean);

    Object sendAndWait(Object bean, long duration, TimeUnit units);
}
