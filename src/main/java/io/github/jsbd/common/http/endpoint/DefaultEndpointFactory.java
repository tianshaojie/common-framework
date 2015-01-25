package io.github.jsbd.common.http.endpoint;

import io.github.jsbd.common.lang.Holder;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.transport.DefaultHolder;
import io.github.jsbd.common.lang.transport.Receiver;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class DefaultEndpointFactory implements EndpointFactory {

  private Receiver                          messageClosure  = null;
  private Holder                            responseContext = new DefaultHolder();
  private Transformer<Object, HttpResponse> responseEncoder = null;

  @Override
  public Endpoint createEndpoint(Channel channel) {
    ServerEndpoint endpoint = new ServerEndpoint();

    endpoint.setChannel(channel);
    endpoint.setMessageClosure(this.messageClosure);
    endpoint.setResponseContext(this.responseContext);
    endpoint.setResponseEncoder(this.responseEncoder);
    endpoint.start();

    return endpoint;
  }

  @Override
  public void setMessageClosure(Receiver messageClosure) {
    this.messageClosure = messageClosure;
  }

  @Override
  public void setResponseContext(Holder responseContext) {
    this.responseContext = responseContext;
  }

  @Override
  public void setResponseEncoder(Transformer<Object, HttpResponse> responseEncoder) {
    this.responseEncoder = responseEncoder;
  }

}
