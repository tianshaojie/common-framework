package io.github.jsbd.common.lang.transport;

public interface Sender {

    void send(Object bean);

    void send(Object object, Receiver receiver);

}