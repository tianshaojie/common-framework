package io.github.jsbd.common.http.endpoint;

import io.github.jsbd.common.http.TransportUtil;
import io.github.jsbd.common.http.response.DefaultHttpResponseSender;
import io.github.jsbd.common.http.response.HttpResponseSender;
import io.github.jsbd.common.lang.Holder;
import io.github.jsbd.common.lang.IpPortPair;
import io.github.jsbd.common.lang.KeyTransformer;
import io.github.jsbd.common.lang.Transformer;
import io.github.jsbd.common.lang.transport.Receiver;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerEndpoint implements Endpoint {

  private static final Logger               logger             = LoggerFactory.getLogger(ServerEndpoint.class);

  private Receiver                          messageClosure     = null;
  private Holder                            responseContext    = null;

  private KeyTransformer                    keyTransformer     = new KeyTransformer();

  private Channel                           channel            = null;
  private HttpResponseSender                httpResponseSender = new DefaultHttpResponseSender();
  private Transformer<Object, HttpResponse> responseEncoder    = null;

  private InetSocketAddress                 addr               = null;

  @Override
  public void send(Object bean) {
    if (null != bean) {
      // get request back.
      Object key = keyTransformer.transform(bean);
      if (key == null) {
        return;
      }

      if (responseContext == null) {
        return;
      }

      HttpRequest req = (HttpRequest) responseContext.getAndRemove(key);
      if (req == null) {
        return;
      }
      TransportUtil.attachRequest(bean, req);

      logger.debug("send msg [{}].", bean);
      doSend(bean);
    }

  }

  @Override
  public void send(Object bean, Receiver receiver) {
    throw new UnsupportedOperationException("not implemented yet!");
  }

  @Override
  public void messageReceived(final Object msg) {
    // save request
    Object key = keyTransformer.transform(msg);
    if (key != null) {
      getResponseContext().put(key, TransportUtil.getRequestOf(msg));
    }

    if (this.messageClosure != null) {
      this.messageClosure.messageReceived(msg);
    }
  }

  @Override
  public void stop() {
    this.responseContext = null;
    this.messageClosure = null;
    this.channel = null;
  }

  @Override
  public void start() {
  }

  private void doSend(Object bean) {
    if (bean != null) {
      HttpResponse response = (HttpResponse) responseEncoder.transform(bean);
      httpResponseSender.sendResponse(channel, response);
    }
  }

  @Override
  public void setChannel(Channel channel) {
    this.channel = channel;
  }

  public void setResponseEncoder(Transformer<Object, HttpResponse> responseEncoder) {
    this.responseEncoder = responseEncoder;
  }

  public void setMessageClosure(Receiver messageClosure) {
    this.messageClosure = messageClosure;
  }

  public void setResponseContext(Holder responseContext) {
    this.responseContext = responseContext;
  }

  public Holder getResponseContext() {
    return responseContext;
  }

  public void setKeyTransformer(KeyTransformer keyTransformer) {
    this.keyTransformer = keyTransformer;
  }

  @Override
  public IpPortPair getRemoteAddress() {
    if (addr != null) {
      return new IpPortPair(this.addr.getAddress().getHostAddress(), this.addr.getPort());
    }

    InetSocketAddress addr = (InetSocketAddress) channel.getRemoteAddress();
    return new IpPortPair(this.addr.getAddress().getHostAddress(), addr.getPort());
  }

  @Override
  public void setAddr(InetSocketAddress addr) {
    this.addr = addr;
  }

}
