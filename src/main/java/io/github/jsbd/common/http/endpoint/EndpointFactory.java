package io.github.jsbd.common.http.endpoint;

import io.github.jsbd.common.lang.Holder;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.transport.Receiver;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface EndpointFactory {

  Endpoint createEndpoint(Channel channel);
  void setMessageClosure(Receiver messageClosure);
  void setResponseContext(Holder responseContext);
  void setResponseEncoder(Transformer<Object, HttpResponse> responseEncoder);
}
